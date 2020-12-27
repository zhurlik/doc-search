package com.github.zhurlik.tika.event;

import org.springframework.context.ApplicationEvent;

public class ElasticSearchEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ElasticSearchEvent(ACTIONS source) {
        super(source);
    }

    public enum ACTIONS {
        INITIALIZE,
        CREATE_INDEX,
        DELETE_INDEX;
    }
}
