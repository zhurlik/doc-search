package com.github.zhurlik.tika.config;

import lombok.Data;
import org.apache.tika.Tika;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * A set of the beans related with Tika and required for scanning process.
 *
 * @author zhurlik@gmail.com
 */
@Configuration
public class TikaConfig {

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    @ConfigurationProperties(prefix = "scanner")
    public ScannerProperties scannerProperties() {
        return new ScannerProperties();
    }

    /**
     * This class will be populated from the application.yml
     */
    @Data
    public static class ScannerProperties {
        /**
         * A list of the resources for scanning files.
         */
        private List<String> resources;
    }
}
