package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.TransactionPayloadTypes;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.TransactionTypes;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private String accountId;
    private double amount;
    private String credentialsId;
    private Date date;
    private String description;
    private Map<String, String> internalPayload;
    private Map<TransactionPayloadTypes, String> payload;
    private boolean pending;
    private TransactionTypes type;
    private String userId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getInternalPayload() {
        if (internalPayload == null) {
            internalPayload = new HashMap<String, String>();
        }

        return internalPayload;
    }

    public void setInternalPayload(String key, String value) {
        getInternalPayload().put(key, value);
    }

    public void setInternalPayload(Map<String, String> internalPayload) {
        this.internalPayload = internalPayload;
    }

    public Map<TransactionPayloadTypes, String> getPayload() {
        return payload;
    }

    public void setPayload(
            Map<TransactionPayloadTypes, String> payload) {
        this.payload = payload;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public TransactionTypes getType() {
        return type;
    }

    public void setType(TransactionTypes type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
