package com.vikthorvergara.pulsar.rpc.model;

import com.vikthorvergara.pulsar.common.model.BaseEvent;

public class RpcResponse extends BaseEvent {

    private String requestId;
    private String result;
    private String error;
    private boolean success;

    public RpcResponse() {
        super();
        this.success = true;
    }

    public RpcResponse(String requestId, String result) {
        this();
        this.requestId = requestId;
        this.result = result;
        this.success = true;
    }

    public RpcResponse(String requestId, String error, boolean success) {
        this();
        this.requestId = requestId;
        this.error = error;
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
