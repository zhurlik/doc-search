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

    private static final int TEN_SECONDS = 10000;
    private static final int ONE_MINUTE = 60000;
    private static final int FIVE_SECONDS = 5000;
    private static final int HALF_MINUTE = 30000;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RestHighLevelClient client;

    /**
     * A cron job to restart the scanning process.
     */
    @Scheduled(initialDelay = TEN_SECONDS, fixedRate = ONE_MINUTE)
    public void rescan() {
        log.info(" Rescanning...");
        applicationEventPublisher.publishEvent(new ScannerEvent(ScannerEvent.ACTIONS.START));
    }

    /**
     * A cron job to check Elasticsearch server.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    @Scheduled(initialDelay = FIVE_SECONDS, fixedRate = HALF_MINUTE)
    public void pingElasticSearch() {
        try {
            final boolean available = client.ping(RequestOptions.DEFAULT);
            log.info(" Ping ElasticSearch server: {}", available);
        } catch (IOException e) {
            log.warn("A problem with ElasticSearch:", e);
        }
    }
}
