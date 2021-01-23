package com.github.zhurlik.tika.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.zhurlik.tika.config.ElasticSearchProperties;
import com.github.zhurlik.tika.event.ElasticSearchDocumentEvent;
import com.github.zhurlik.tika.event.ElasticSearchEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This class includes 2 listeners for handling ElasticSearch events.
 * See
 * {@link ElasticSearchDocumentEvent}
 * {@link ElasticSearchEvent}
 *
 * @author zhurlik@gmail.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class ElasticSearchListener {
    public static final int TEN = 10;
    private final ElasticSearchProperties elasticSearchProperties;
    private final String indexMappings;
    private final RestHighLevelClient client;
    private final WebClient tikaWebClient;

    @Value("${tika.ocr.langs:'eng'}")
    private final String ocrSupportedLanguages;

    /**
     * Listens to {@link ElasticSearchEvent} for creating an index in Elasticsearch.
     *
     * @param event ElasticSearchEvent
     */
    @EventListener
    public void handle(final ElasticSearchEvent event) {
        final ElasticSearchEvent.ACTIONS action = (ElasticSearchEvent.ACTIONS) event.getSource();
        log.info("ElasticSearchEvent:{}", action);
        initIndex();
    }

    /**
     * Listens to {@link ElasticSearchDocumentEvent} for indexing and storing a document in Elasticsearch.
     * @param event ElasticSearchDocumentEvent
     */
    @EventListener
    public void handleDocument(final ElasticSearchDocumentEvent event) {
        final ImmutablePair<ElasticSearchDocumentEvent.ACTIONS, Path> pair =
                (ImmutablePair<ElasticSearchDocumentEvent.ACTIONS, Path>) event.getSource();
        indexDocument(pair.getValue());
    }

    private void indexDocument(final Path path) {
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            // to be able to read twice
            in.mark(Integer.MAX_VALUE);
            final String md5Sum = DigestUtils.md5DigestAsHex(in);

            // Tika Resource also accepts the files as multipart/form-data attachments with POST
            final JsonNode tikaResponse = tikaWebClient.put()
                    .uri("/rmeta/text")
                    .header("X-Tika-OCRLanguage", ocrSupportedLanguages)
                    .header("X-Tika-PDFOcrStrategy","ocr_only")
                    .body(BodyInserters.fromResource(new FileSystemResource(path)))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (!isDocumentExist(md5Sum)) {
                in.reset();
                // second time of reading
                log.info(" File: {}, MD5: {}", path, md5Sum);
                store(md5Sum, path, tikaResponse);
            }
        } catch (Exception e) {
            log.warn("A problem with reading:", e);
        }
    }

    private boolean isDocumentExist(final String md5Sum) {
        final GetRequest getRequest = new GetRequest(elasticSearchProperties.getIndex().getName(), md5Sum);
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        try {
            return client.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("A problem with checking a document in the ElasticSearch:", e);
            return false;
            // TODO: retry?
        }
    }

    private void store(final String md5Sum, final Path path, final JsonNode tikaResponse) {
        final IndexRequest request = new IndexRequest(elasticSearchProperties.getIndex().getName());
        final Map<String, Object> jsonMap = buildJsonMap(path, tikaResponse);

        request.id(md5Sum);
        request.source(jsonMap);
        // TODO: request.version(); request.versionType(VersionType.EXTERNAL);

        try {
            final IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            log.info(" ElasticSearch response: {}", indexResponse);
        } catch (IOException e) {
            log.error("A problem with ElasticSearch:", e);
            // TODO: retry?
        }
    }

    private Map<String, Object> buildJsonMap(final Path path, final JsonNode tikaResponse) {
        // data
        final Map<String, Object> jsonMap = new HashMap<>(3);
        final JsonNode tikaMeta = tikaResponse.get(0);
        final Map<String, Object> documentDetails = new HashMap<>(tikaMeta.size());
        final String content = Optional.ofNullable(
                ((ObjectNode) tikaMeta).remove("X-TIKA:content"))
                .map(JsonNode::asText)
                .map(String::trim)
                .orElse("");

        jsonMap.put("content", content);
        jsonMap.put("path", path);
        jsonMap.put("document-details", documentDetails);

        tikaMeta.fieldNames()
                .forEachRemaining(s -> documentDetails.put(s, tikaMeta.get(s)));

        return jsonMap;
    }

    private void deleteIndex() {
        try {
            final DeleteIndexRequest request = new DeleteIndexRequest(getIndexName());
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            final AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
            log.info("The index: {} has been deleted. {}", getIndexName(), deleteIndexResponse);
        } catch (IOException e) {
            log.error("A problem with index deletion:", e);
        }
    }

    private void createIndex() {
        try {
            final CreateIndexRequest request = new CreateIndexRequest(getIndexName());
            request.mapping(indexMappings, XContentType.JSON);
            final CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            log.info("The index: {} has been created. {}", getIndexName(), createIndexResponse);
        } catch (IOException e) {
            log.error("A problem with index creation:", e);
        }
    }

    private void initIndex() {
        try {
            final GetIndexRequest request = new GetIndexRequest(getIndexName());
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

            if (!exists) {
                createIndex();
                return;
            }

            if (elasticSearchProperties.getIndex().isRecreate()) {
                deleteIndex();
                createIndex();
            }
        } catch (IOException e) {
            log.error("A problem with finding of the index:", e);
            attempt(this::initIndex);
        }
    }

    /**
     * Retry to call API when ElasticSearch still is not up.
     *
     * @param action any call
     */
    private void attempt(final Runnable action) {
        try {
            TimeUnit.SECONDS.sleep(TEN);
            action.run();
        } catch (Exception e) {
            log.error("A problem with connection:", e);
        }
    }

    private String getIndexName() {
        return elasticSearchProperties.getIndex().getName();
    }
}
