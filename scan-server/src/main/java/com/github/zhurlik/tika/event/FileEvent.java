package com.github.zhurlik.tika.event;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * @author zhurlik@gmail.com
 */
public class FileEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public FileEvent(final Path source) {
        super(source);
    }
}
