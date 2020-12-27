package com.github.zhurlik.tika.task;


import com.github.zhurlik.tika.event.ScannerEvent;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class ScheduledTasksTest {

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RestHighLevelClient client;

    @Test
    void testMain() {
        scheduledTasks.rescan();
        verify(applicationEventPublisher).publishEvent(isA(ScannerEvent.class));
    }

    @Test
    void testPing() {
        assertThrows(NullPointerException.class, () -> scheduledTasks.pingElasticSearch());
    }
}
