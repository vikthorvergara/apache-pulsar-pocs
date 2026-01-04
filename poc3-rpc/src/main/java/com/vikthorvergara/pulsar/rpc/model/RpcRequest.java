package com.vikthorvergara.pulsar.rpc.model;

import com.vikthorvergara.pulsar.common.model.BaseEvent;

public class RpcRequest extends BaseEvent {

    private String requestId;
    private String operation;
    private String payload;
    private String replyTo;

    public RpcRequest() {
        super();
    }

    public RpcRequest(String requestId, String operation, String payload, String replyTo) {
        this();
        this.requestId = requestId;
        this.operation = operation;
        this.payload = payload;
        this.replyTo = replyTo;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
