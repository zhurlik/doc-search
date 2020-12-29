package com.github.zhurlik.tika.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author zhurlik@gmail.com
 */
@Configuration
@Slf4j
public class ElasticSearchConfig {
    @Bean
    @ConfigurationProperties(prefix = "elasticsearch")
    public ElasticSearchProperties elasticSearchProperties() {
        return new ElasticSearchProperties();
    }

    @Bean
    public RestHighLevelClient elasticsearch(final ElasticSearchProperties elasticSearchProperties) {
        final HttpHost httpHost = new HttpHost(elasticSearchProperties.getHost(),
                elasticSearchProperties.getPort(),
                elasticSearchProperties.getSchema());
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }

    @Bean
    public DisposableBean stopClient(final RestHighLevelClient elasticsearch) {
        return () -> {
            log.info(" Stopping ElasticSearch Client...");
            elasticsearch.close();
        };
    }

    @Bean
    public String indexMappings(final ResourceLoader resourceLoader, final ElasticSearchProperties elasticSearchProperties) {
        return asString(resourceLoader.getResource(elasticSearchProperties.getIndex().getMappingsPath()));
    }

    private String asString(final Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
