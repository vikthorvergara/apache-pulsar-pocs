package com.vikthorvergara.pulsar.taskqueue.model;

import com.vikthorvergara.pulsar.common.model.BaseEvent;

public class Task extends BaseEvent {

    private String taskType;
    private String payload;
    private int priority;
    private String status;
    private int retryCount;

    public Task() {
        super();
        this.priority = 5;
        this.status = "PENDING";
        this.retryCount = 0;
    }

    public Task(String taskType, String payload, int priority) {
        this();
        this.taskType = taskType;
        this.payload = payload;
        this.priority = priority;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
