package com.github.zhurlik.tika.event;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

public class ElasticSearchDocumentEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ElasticSearchDocumentEvent(final ImmutablePair<ACTIONS, Path> source) {
        super(source);
    }

    public enum ACTIONS {
        STORE_DOCUMENT;
    }
}
