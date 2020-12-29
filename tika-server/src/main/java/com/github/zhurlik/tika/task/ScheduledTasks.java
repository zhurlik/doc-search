package com.github.zhurlik.tika.task;

import com.github.zhurlik.tika.event.ScannerEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author zhurlik@gmail.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class ScheduledTasks {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RestHighLevelClient client;

    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    public void rescan() {
        log.info(" Rescanning...");
        applicationEventPublisher.publishEvent(new ScannerEvent(ScannerEvent.ACTIONS.START));
    }

    @Scheduled(initialDelay = 5000, fixedRate = 30000)
    public void pingElasticSearch() {
        try {
            final boolean available = client.ping(RequestOptions.DEFAULT);
            log.info(" Ping ElasticSearch server: {}", available);
        } catch (IOException e) {
            log.warn("A problem with ElasticSearch:", e);
        }
    }
}
