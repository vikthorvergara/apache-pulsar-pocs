package com.vikthorvergara.pulsar.common.model;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseEvent {

    private String id;
    private Instant timestamp;
    private String type;

    public BaseEvent() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.type = this.getClass().getSimpleName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
