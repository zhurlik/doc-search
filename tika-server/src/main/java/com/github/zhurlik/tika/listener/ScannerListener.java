package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.event.ElasticSearchDocumentEvent;
import com.github.zhurlik.tika.event.FileEvent;
import com.github.zhurlik.tika.event.ScannerEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
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
            Flux.fromStream(Files.walk(dir, FileVisitOption.FOLLOW_LINKS))
                    // only files with a content
                    .filter(path -> path.toFile().isFile() && path.toFile().length() > 0)
                    .map(FileEvent::new)
                    .subscribe(applicationEventPublisher::publishEvent);
        } catch (IOException e) {
            log.warn("A problem with scanning:", e);
        }
    }
}
