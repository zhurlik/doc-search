package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.config.ElasticSearchProperties;
import com.github.zhurlik.tika.config.IndexProperties;
import com.github.zhurlik.tika.event.ElasticSearchEvent;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class ElasticSearchListenerTest {
    @InjectMocks
    private ElasticSearchListener elasticSearchListener;

    @Mock
    private ElasticSearchProperties elasticSearchProperties;

    private String indexMappings = "{}";

    @Mock
    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(elasticSearchListener, "indexMappings", indexMappings);
    }

    @Test
    void testHandleNull() {
        assertThrows(NullPointerException.class, () -> elasticSearchListener.handle(null));
    }

    @Test
    void testHandleInit() throws Exception {
        // Given
        final IndexProperties indexProperties = mock(IndexProperties.class);
        when(elasticSearchProperties.getIndex()).thenReturn(indexProperties);
        when(indexProperties.getName()).thenReturn("test-index");

        // no way to mock ElasticSearch clients
        assertThrows(NullPointerException.class, () -> elasticSearchListener.handle(new ElasticSearchEvent(ElasticSearchEvent.ACTIONS.INITIALIZE)));
    }

    @Test
    void testHandleCreate() {
        // Given
        final IndexProperties indexProperties = mock(IndexProperties.class);
        when(elasticSearchProperties.getIndex()).thenReturn(indexProperties);
        when(indexProperties.getName()).thenReturn("test-index");

        // no way to mock ElasticSearch clients
        assertThrows(NullPointerException.class, () -> elasticSearchListener.handle(new ElasticSearchEvent(ElasticSearchEvent.ACTIONS.CREATE_INDEX)));
    }

    @Test
    void testHandleDelete() throws Exception {
        // Given
        final IndexProperties indexProperties = mock(IndexProperties.class);
        when(elasticSearchProperties.getIndex()).thenReturn(indexProperties);
        when(indexProperties.getName()).thenReturn("test-index");

        // no way to mock ElasticSearch clients
        assertThrows(NullPointerException.class, () -> elasticSearchListener.handle(new ElasticSearchEvent(ElasticSearchEvent.ACTIONS.DELETE_INDEX)));
    }
}
