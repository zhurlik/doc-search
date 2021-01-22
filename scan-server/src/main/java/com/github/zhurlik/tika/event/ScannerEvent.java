package com.github.zhurlik.tika.event;

import org.springframework.context.ApplicationEvent;

/**
 * This class defines the events for scanning process.
 *
 * @author zhurlik@gmail.com
 */
public class ScannerEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ScannerEvent(final ACTIONS source) {
        super(source);
    }

    public enum ACTIONS {
        START,
        STOP;
    }
}
