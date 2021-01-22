package com.github.zhurlik.tika;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zhurlik@gmail.com
 */
@SpringBootApplication
@Slf4j
@EnableScheduling
@EnableAsync
public class DocSearchApplication implements CommandLineRunner {

    /**
     * A start point.
     *
     * @param args
     */
    public static void main(final String[] args) {
        SpringApplication.run(DocSearchApplication.class, args);
    }

    @Override
    public void run(final String... args) {
        log.info("The application has been started...");
    }
}
