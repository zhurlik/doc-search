package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.event.ElasticSearchEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author zhurlik@gmail.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class ApplicationListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * When Spring is up and running.
     *
     * @param event
     */
    @EventListener
    public void up(final ContextRefreshedEvent event) {
        applicationEventPublisher.publishEvent(new ElasticSearchEvent(ElasticSearchEvent.ACTIONS.INITIALIZE));
    }
}
