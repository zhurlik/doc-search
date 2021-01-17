package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.config.ScannerProperties;
import com.github.zhurlik.tika.event.ElasticSearchDocumentEvent;
import com.github.zhurlik.tika.event.FileEvent;
import com.github.zhurlik.tika.event.ScannerEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;


/**
 * @author zhurlik@gmail.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class ScannerListener {

    private final ScannerProperties scannerProperties;
    private final ResourceLoader resourceLoader;

    private final List<Path> dirs;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Listens to {@link ScannerEvent} for beginning the scanning process.
     *
     * @param event to start scanning
     */
    @EventListener
    @Async
    public void scanDirs(final ScannerEvent event) {
        log.info(" Scanner event: {}, time: {}, thread: {}", event.getSource(), Instant.now(), Thread.currentThread());
        dirs.forEach(this::doScan);
    }

    /**
     * Listens to {@link FileEvent}.
     *
     * @param event file event
     */
    @EventListener
    public void handleFile(final FileEvent event) {
        final Path path = (Path) event.getSource();
        applicationEventPublisher.publishEvent(new ElasticSearchDocumentEvent(
                new ImmutablePair(ElasticSearchDocumentEvent.ACTIONS.STORE_DOCUMENT, path)));
    }

    private void doScan(final Path dir) {
        log.info(" Scan dir: {}", dir.toString());
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().isFile() && Files.size(file) > 0) {
                        // TODO: file attributes
                        applicationEventPublisher.publishEvent(new FileEvent(file));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("A problem with scanning:", e);
        }
    }
}
