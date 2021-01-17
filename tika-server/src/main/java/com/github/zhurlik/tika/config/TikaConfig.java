package com.github.zhurlik.tika.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A set of the beans related with Tika and required for scanning process.
 *
 * @author zhurlik@gmail.com
 */
@Configuration
@Slf4j
public class TikaConfig {

    /**
     * Returns Tika instance.
     *
     * @return instance of {@link Tika}
     */
    @Bean
    public Tika tika() {
        return new Tika();
    }

    /**
     * Returns the properties for the scanner.
     *
     * @return the properties from the config files
     */
    @Bean
    @ConfigurationProperties(prefix = "scanner")
    public ScannerProperties scannerProperties() {
        return new ScannerProperties();
    }

    /**
     * Returns a list of dirs that should be scanned.
     *
     * @param scannerProperties
     * @param resourceLoader
     * @return a list of dirs
     */
    @Bean
    public List<Path> dirs(final ScannerProperties scannerProperties, final ResourceLoader resourceLoader) {
        return scannerProperties.getResources().stream()
                .map(resourceLoader::getResource)
                .map(resource -> {
                    try {
                        return resource.getFile();
                    } catch (IOException e) {
                        log.warn("A problem with resource:", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(File::isDirectory)
                .map(File::toPath)
                .collect(Collectors.toList());
    }
}
