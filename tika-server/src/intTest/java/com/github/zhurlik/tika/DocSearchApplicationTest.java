package com.github.zhurlik.tika;


import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@Slf4j
@ContextConfiguration(initializers = DocSearchApplicationTest.Initializer.class)
class DocSearchApplicationTest {

    @Container
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.10.1")
                    .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()));

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "elasticsearch.port=" + ELASTICSEARCH_CONTAINER.getFirstMappedPort(),
                    "scanner.resources[0]=classpath:/docs"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private RestHighLevelClient client;

    @AfterAll
    static void afterAll() {
        ELASTICSEARCH_CONTAINER.stop();
    }

    @Test
    void testElasticSearch() throws Exception {
        assertTrue(client.ping(RequestOptions.DEFAULT));
    }

    @Test
    void testIndex() throws Exception {
        final GetIndexRequest request = new GetIndexRequest("documents");
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);

        assertTrue(client.indices().exists(request, RequestOptions.DEFAULT));
    }

    @Test
    void testSearch() throws Exception {
        // when the documents will be indexed
        TimeUnit.SECONDS.sleep(15);

        final CountRequest countRequest = new CountRequest();
        countRequest.query(QueryBuilders.matchAllQuery());
        final CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        assertEquals(5, countResponse.getCount());
    }
}
