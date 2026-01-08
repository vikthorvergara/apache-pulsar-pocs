package com.vikthorvergara.pulsar.unified.model;

import com.vikthorvergara.pulsar.common.model.BaseEvent;

import java.math.BigDecimal;

public class Payment extends BaseEvent {

    private String merchantId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String description;

    public Payment() {
        super();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + getId() +
                ", merchantId='" + merchantId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
