package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.config.ElasticSearchProperties;
import com.github.zhurlik.tika.event.ElasticSearchDocumentEvent;
import com.github.zhurlik.tika.event.ElasticSearchEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class ElasticSearchListener {
    private final ElasticSearchProperties elasticSearchProperties;
    private final String indexMappings;
    private final RestHighLevelClient client;
    private final Tika tika;

    @EventListener
    public void handle(final ElasticSearchEvent event) {
        final ElasticSearchEvent.ACTIONS action = (ElasticSearchEvent.ACTIONS) event.getSource();
        switch (action) {
            case INITIALIZE:
                init();
                break;
            default: throw new UnsupportedOperationException("Not implemented yet or unsupported");
        }
    }

    @EventListener
    public void handleDocument(final ElasticSearchDocumentEvent event) {
        final ImmutablePair<ElasticSearchDocumentEvent.ACTIONS, Path> pair =
                (ImmutablePair<ElasticSearchDocumentEvent.ACTIONS, Path>) event.getSource();
        final Path path = pair.getValue();

        try (final BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            // to be able to read twice
            in.mark(Integer.MAX_VALUE);
            final String md5Sum = DigestUtils.md5DigestAsHex(in);

            if (!isDocumentExist(md5Sum)){
                in.reset();
                // second time of reading
                final Metadata metadata = new Metadata();
                final String body = tika.parseToString(in, metadata);
                log.info(">> File: {}, MD5: {}, Body:\n{}", path, md5Sum, body);
                store(md5Sum, path, metadata, body);
            }
        } catch (IOException e) {
            log.warn("A problem with reading:", e);
        } catch (TikaException e) {
            log.warn("A problem with parsing:", e);
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
        }
    }

    private void store(final String md5Sum, final Path path, final Metadata metadata, final String body) {
        final IndexRequest request = new IndexRequest(elasticSearchProperties.getIndex().getName());
        request.id(md5Sum);

        // data
        final Map<String, Object> jsonMap = new HashMap<>(3);
        final Map<String, Object> documentDetails = new HashMap<>(metadata.names().length);
        jsonMap.put("content", body);
        jsonMap.put("path", path);
        jsonMap.put("document-details", documentDetails);

        Arrays.stream(metadata.names())
                .forEach(n -> documentDetails.put(n, metadata.get(n)));

        request.source(jsonMap);

        // TODO: request.version(); request.versionType(VersionType.EXTERNAL);

        try {
            final IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            log.info(">> ElasticSearch response: {}", indexResponse);
        } catch (IOException e) {
            log.error("A problem with ElasticSearch:", e);
        }
    }

    private void delete() {
        try {
            final DeleteIndexRequest request = new DeleteIndexRequest(getIndexName());
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            final AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
            log.info("The index: {} has been deleted. {}", getIndexName(), deleteIndexResponse);
        } catch (IOException e) {
            log.error("A problem with index deletion:", e);
        }
    }

    private void create() {
        try {
            final CreateIndexRequest request = new CreateIndexRequest(getIndexName());
            request.mapping(indexMappings, XContentType.JSON);
            final CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            log.info("The index: {} has been created. {}", getIndexName(), createIndexResponse);
        } catch (IOException e) {
            log.error("A problem with index creation:", e);
        }
    }

    private void init() {
        try {
            final GetIndexRequest request = new GetIndexRequest(getIndexName());
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

            if (!exists) {
                create();
                return;
            }

            if (elasticSearchProperties.getIndex().isRecreate()) {
                delete();
                create();
            }
        } catch (IOException e) {
            log.error("A problem with finding of the index:", e);
            attempt();
        }
    }

    /**
     * Retry to connect when ElasticSearch still is not up.
     */
    private void attempt() {
        try {
            TimeUnit.SECONDS.sleep(10);
            init();
        } catch (Exception e) {
            log.error("A problem with connection:", e);
        }
    }

    private String getIndexName() {
        return elasticSearchProperties.getIndex().getName();
    }
}
