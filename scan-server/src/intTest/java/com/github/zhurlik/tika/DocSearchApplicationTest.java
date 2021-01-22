package com.github.zhurlik.tika;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@Slf4j
@ContextConfiguration(initializers = DocSearchApplicationTest.Initializer.class)
class DocSearchApplicationTest {
    @Container
    private static final GenericContainer TIKA_OCR_CONTAINER = new GenericContainer(
            new ImageFromDockerfile()
                    .withFileFromPath(".", Paths.get("../tika-ocr-server"))
    ).withExposedPorts(9998);

    @Container
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.10.1")
                    .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()));

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(final ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "elasticsearch.port=" + ELASTICSEARCH_CONTAINER.getFirstMappedPort(),
                    "scanner.resources[0]=classpath:/docs",
                    "tika.url=http://localhost:" + TIKA_OCR_CONTAINER.getFirstMappedPort()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private RestHighLevelClient client;

    @Value("${tika.url}")
    private String tikaOcrUrl;

    @Autowired
    private ObjectMapper jackson;

    @AfterAll
    static void afterAll() {
        ELASTICSEARCH_CONTAINER.stop();
        TIKA_OCR_CONTAINER.stop();
    }

    @Test
    @Order(1)
    void testElasticSearch() throws Exception {
        assertTrue(client.ping(RequestOptions.DEFAULT));
    }

    @Test
    @Order(2)
    void testIndex() throws Exception {
        final GetIndexRequest request = new GetIndexRequest("documents");
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);

        assertTrue(client.indices().exists(request, RequestOptions.DEFAULT));
    }

    @Test
    @Order(3)
    void testTikaOcr() throws Exception {
        final HttpUriRequest request = new HttpGet(tikaOcrUrl + "/version");
        final HttpResponse response = HttpClientBuilder
                .create()
                .build()
                .execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Apache Tika 1.25", IOUtils.toString(response.getEntity().getContent(),
                StandardCharsets.UTF_8));
    }

    @Test
    @Order(4)
    void testAllIndexedDocs() throws Exception {
        // when the documents will be indexed
        TimeUnit.SECONDS.sleep(25);

        final SearchRequest searchRequest = new SearchRequest("documents");
        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        final SearchHit[] hits = searchResponse.getHits().getHits();

        assertEquals(7, hits.length);
        for (final SearchHit h : hits) {
            final JsonNode doc = jackson.readValue(h.toString(), JsonNode.class);
            final JsonNode src = doc.get("_source");
            final String fileName = Paths.get(src.get("path").asText()).getFileName().toString();
            final String content = src.get("content").asText();

            switch (fileName) {
                case "Changing_Fields_Resume.odt":
                    assertTrue(content.contains("Coordinated employee stations so as to minimize delays and long waits"));
                    break;
                case "sample2.docx":
                    assertTrue(content.contains("Sit sane ista voluptas. Non quam nostram quidem, inquit Pomponius iocans; An tu me de L."));
                    break;
                case "схема-посадки.pot":
                    assertTrue(content.contains("В презентацию также можно вставить объекты из меню «Автофигуры» на панели задач «Рисование»."));
                    break;
                case "резюме.dotx":
                    assertTrue(content.contains("Перечислите свои сильные стороны, имеющие отношение к должности, на которую претендуете"));
                    break;
                case "alegro-pl.png":
                    assertTrue(content.contains("zbudować w przykładzie takim gdy koszty są za ważne w..."));
                    break;
                case "bel.pdf":
                    assertTrue(content.contains("5. Флэш-гульні \"Вучымся чытаць па-беларуску\""));
                    break;
                case "sample.pdf":
                    assertTrue(content.contains("Oh, how boring typing this stuff. But not as boring as watching"));
                    break;
            }
        }
    }
}
