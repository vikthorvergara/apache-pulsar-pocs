package com.vikthorvergara.pulsar.messaging.model;

import com.vikthorvergara.pulsar.common.model.BaseEvent;

public class TenantEvent extends BaseEvent {

    private String tenantId;
    private String data;
    private String eventName;

    public TenantEvent() {
        super();
    }

    public TenantEvent(String tenantId, String eventName, String data) {
        super();
        this.tenantId = tenantId;
        this.eventName = eventName;
        this.data = data;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return "TenantEvent{" +
                "id='" + getId() + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", data='" + data + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
