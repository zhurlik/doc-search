package com.github.zhurlik.tika.listener;

import com.github.zhurlik.tika.config.TikaConfig;
import com.github.zhurlik.tika.event.ScannerEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


/**
 * @author zhurlik@gmail.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class ScannerListener {

    private final TikaConfig.ScannerProperties scannerProperties;
    private final ResourceLoader resourceLoader;
    private final Tika tika;

    @EventListener
    public void handle(final ScannerEvent event) {
        log.info(">> Scanner event: {}", event.getSource());
        scannerProperties.getResources().stream()
                .map(resourceLoader::getResource)
                // TODO: rework for folders....
                .forEach(r -> {
                    try {
                        final String text = tika.parseToString(r.getURL());
                        log.info(">> Extracted text:\n{}", text);
                    } catch (Exception e) {
                        log.error("A problem during extracting:", e);
                    }
                });
    }
}
