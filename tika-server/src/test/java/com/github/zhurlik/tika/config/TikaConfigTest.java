package com.github.zhurlik.tika.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class TikaConfigTest {
    @InjectMocks
    private TikaConfig tikaConfig;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ScannerProperties scannerProperties;

    @Test
    void testEmptyList() {
        when(scannerProperties.getResources()).thenReturn(Collections.emptyList());

        assertTrue(tikaConfig.dirs(scannerProperties, resourceLoader).isEmpty());

        verify(resourceLoader, never()).getResource(anyString());
    }

    @Test
    void testWrongResource() throws Exception {
        // Given
        final Resource resource = mock(Resource.class);
        when(scannerProperties.getResources()).thenReturn(Collections.singletonList("wrong url"));
        when(resourceLoader.getResource("wrong url")).thenReturn(resource);
        when(resource.getFile()).thenThrow(new IOException("test issue"));

        // When
        final List<Path> dirs = tikaConfig.dirs(scannerProperties, resourceLoader);

        // Then
        assertTrue(dirs.isEmpty());
    }

    @Test
    void testWhenIsFile() throws Exception {
        // Given
        final Resource resource = mock(Resource.class);
        final File file = mock(File.class);
        when(scannerProperties.getResources()).thenReturn(Collections.singletonList("link to file"));
        when(resourceLoader.getResource("link to file")).thenReturn(resource);
        when(resource.getFile()).thenReturn(file);
        when(file.isDirectory()).thenReturn(false);

        // When
        final List<Path> dirs = tikaConfig.dirs(scannerProperties, resourceLoader);

        // Then
        assertTrue(dirs.isEmpty());
    }

    @Test
    void testDirs() throws Exception {
        // Given
        final Resource resource = mock(Resource.class);
        final File file = mock(File.class);
        when(scannerProperties.getResources()).thenReturn(Arrays.asList("dir 1", "dir 2"));
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getFile()).thenReturn(file);
        when(file.isDirectory())
                .thenReturn(true)
                .thenReturn(true);
        when(file.toPath()).thenReturn(mock(Path.class));

        // When
        final List<Path> dirs = tikaConfig.dirs(scannerProperties, resourceLoader);

        // Then
        assertTrue(dirs.size() == 2);
        assertNotNull(dirs.get(0));
        assertNotNull(dirs.get(1));
    }

    @Test
    void testTika() {
        assertNotNull(tikaConfig.tika());
    }

    @Test
    void testScannerProperties() {
        final ScannerProperties scannerProperties = tikaConfig.scannerProperties();
        assertNotNull(scannerProperties);
        assertTrue(scannerProperties.getResources().isEmpty());
    }
}