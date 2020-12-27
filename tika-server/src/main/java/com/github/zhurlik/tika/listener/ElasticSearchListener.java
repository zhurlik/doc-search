package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.config.ElasticSearchProperties;
import com.github.zhurlik.tika.event.ElasticSearchEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class ElasticSearchListener {
    private final ElasticSearchProperties elasticSearchProperties;
    private final String indexMappings;
    private final RestHighLevelClient client;

    @EventListener
    public void handle(final ElasticSearchEvent event) {
        final ElasticSearchEvent.ACTIONS action = (ElasticSearchEvent.ACTIONS) event.getSource();
        switch (action) {
            case INITIALIZE:
                init();
                break;
            case CREATE_INDEX:
                create();
                break;
            case DELETE_INDEX:
                delete();
                break;
            default: throw new UnsupportedOperationException("Not implemented yet or unsupported");
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
