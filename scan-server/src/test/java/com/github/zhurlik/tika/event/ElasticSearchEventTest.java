package com.github.zhurlik.tika.event;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author zhurlik@gmail.com
 */
class ElasticSearchEventTest {
    @Test
    void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new ElasticSearchEvent(null));
    }

    @Test
    void testConstructor() {
        assertNotNull(new ElasticSearchEvent(ElasticSearchEvent.ACTIONS.INITIALIZE));
    }

    @Test
    void testActions() {
        assertEquals("INITIALIZE", String.join(",",
                Arrays.stream(ElasticSearchEvent.ACTIONS.values()).map(Enum::toString).collect(Collectors.toList())
                )
        );
    }
}
