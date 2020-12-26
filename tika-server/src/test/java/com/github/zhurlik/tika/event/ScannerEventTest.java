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
class ScannerEventTest {

    @Test
    void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new ScannerEvent(null));
    }

    @Test
    void testConstructor() {
        assertNotNull(new ScannerEvent(ScannerEvent.ACTIONS.START));
    }

    @Test
    void testActions() {
        assertEquals("START,STOP", String.join(",",
                Arrays.stream(ScannerEvent.ACTIONS.values()).map(Enum::toString).collect(Collectors.toList())
                )
        );
    }
}