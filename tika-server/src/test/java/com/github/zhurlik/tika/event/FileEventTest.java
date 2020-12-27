package com.github.zhurlik.tika.event;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author zhurlik@gmail.com
 */
class FileEventTest {

    @Test
    void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new FileEvent(null));
    }

    @Test
    void testConstructor() {
        assertNotNull(new FileEvent(Paths.get("test")));
    }
}
