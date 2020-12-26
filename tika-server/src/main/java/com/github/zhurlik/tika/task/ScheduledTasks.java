package com.github.zhurlik.tika.task;

import com.github.zhurlik.tika.event.ScannerEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhurlik@gmail.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class ScheduledTasks {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(initialDelay = 5000, fixedRate = 60000)
    public void rescan() {
        log.info(">> Rescanning...");
        applicationEventPublisher.publishEvent(new ScannerEvent(ScannerEvent.ACTIONS.START));
    }
}
