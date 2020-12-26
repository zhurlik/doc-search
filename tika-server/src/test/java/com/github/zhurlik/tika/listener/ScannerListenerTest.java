package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.event.FileEvent;
import com.github.zhurlik.tika.event.ScannerEvent;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.BufferedInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class ScannerListenerTest {

    @InjectMocks
    private ScannerListener scannerListener;

    @Spy
    private List<Path> dirs = new ArrayList<>();

    @Spy
    private Tika tika;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void testFile() throws Exception {
        final Path path = Paths.get(this.getClass().getClassLoader().getResource("docs/test.txt").toURI());
        scannerListener.handleFile(new FileEvent(path));
        verify(tika).parseToString(any(BufferedInputStream.class), any(Metadata.class));
    }

    @Test
    void testWrongFile() throws Exception {
        final Path path = Paths.get("a wrong file");
        scannerListener.handleFile(new FileEvent(path));
        verify(tika, never()).parseToString(any(BufferedInputStream.class), any(Metadata.class));
    }

    @Test
    void testRealFolder() throws Exception {
        final Path realDir = Paths.get(this.getClass().getClassLoader().getResource("docs/").toURI());
        dirs.add(realDir);
        scannerListener.scanDirs(new ScannerEvent(ScannerEvent.ACTIONS.START));

        verify(applicationEventPublisher, times(4)).publishEvent(any(FileEvent.class));
    }
}
