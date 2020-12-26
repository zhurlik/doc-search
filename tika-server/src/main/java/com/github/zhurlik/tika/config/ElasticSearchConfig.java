package com.github.zhurlik.tika.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhurlik@gmail.com
 */
@Configuration
@Slf4j
public class ElasticSearchConfig {
    @Bean
    public RestHighLevelClient elasticsearch(){
        return new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }

    @Bean
    public DisposableBean stopClient(final RestHighLevelClient elasticsearch) {
        return () -> {
            log.info(">> Stopping ElasticSearch Client...");
            elasticsearch.close();
        };
    }
}
