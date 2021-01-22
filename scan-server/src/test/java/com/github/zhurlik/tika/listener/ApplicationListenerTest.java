package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.event.ElasticSearchEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class ApplicationListenerTest {
    @InjectMocks
    private ApplicationListener applicationListener;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void testUp() {
        applicationListener.up(mock(ContextRefreshedEvent.class));
        verify(applicationEventPublisher).publishEvent(any(ElasticSearchEvent.class));
    }
}
