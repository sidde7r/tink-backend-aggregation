package se.tink.backend.system.rpc;

import java.util.List;

public class ProcessTransactionsRequest {
    public static final String LOCK_PREFIX_USER = "/locks/processTransactions/user/";

    private String credentialsId;
    private List<Transaction> transactions;
    private String userId;
    private String topic;
    private boolean userTriggered = false;

    public String getCredentialsId() {
        return credentialsId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getUserId() {
        return userId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    public boolean isUserTriggered() {
        return userTriggered;
    }

    public void setUserTriggered(boolean userTriggered) {
        this.userTriggered = userTriggered;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
