package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.event.FileEvent;
import com.github.zhurlik.tika.event.ScannerEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
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

    private final List<Path> dirs;
    private final Tika tika;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    @Async
    public void scanDirs(final ScannerEvent event) {
        log.info(">> Scanner event: {}, time: {}, thread: {}", event.getSource(), Instant.now(), Thread.currentThread());
        dirs.forEach(this::doScan);
    }

    @EventListener
    @Async
    public void handleFile(final FileEvent event) {
        final Path path = (Path) event.getSource();
        log.info(">> File: {}, thread: {}", path, Thread.currentThread());

        try (final BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            final Metadata metadata = new Metadata();
            final String body = tika.parseToString(in, metadata);
            log.info(">> Body:\n{}", body);
            // TODO: sending to elasticsearch
        } catch (IOException e) {
            log.warn("A problem with reading:", e);
        } catch (TikaException e) {
            log.warn("A problem with parsing:", e);
        }
    }

    private void doScan(final Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (Files.size(file) > 0) {
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
