package com.vikthorvergara.pulsar.missioncritical.model;

import com.vikthorvergara.pulsar.common.model.BaseEvent;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment extends BaseEvent {

    private String merchantId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private int retryCount;
    private int maxRetries;
    private String description;
    private String failureReason;
    private Instant lastAttemptTimestamp;
    private Instant completionTimestamp;

    public Payment() {
        super();
        this.status = "PENDING";
        this.retryCount = 0;
        this.maxRetries = 3;
        this.currency = "USD";
    }

    public Payment(String merchantId, String customerId, BigDecimal amount, String description) {
        this();
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
        this.description = description;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getLastAttemptTimestamp() {
        return lastAttemptTimestamp;
    }

    public void setLastAttemptTimestamp(Instant lastAttemptTimestamp) {
        this.lastAttemptTimestamp = lastAttemptTimestamp;
    }

    public Instant getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(Instant completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + getId() +
                ", merchantId='" + merchantId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", retryCount=" + retryCount +
                ", description='" + description + '\'' +
                '}';
    }
}
