package com.lwansbrough.serverfarm.core.services.message;

import org.springframework.context.ApplicationEvent;

public class MessageEvent<T> extends ApplicationEvent {
    private T data;
 
    public MessageEvent(Object source, T data) {
        super(source);
        this.data = data;
    }
    public T getData() {
        return data;
    }
}
