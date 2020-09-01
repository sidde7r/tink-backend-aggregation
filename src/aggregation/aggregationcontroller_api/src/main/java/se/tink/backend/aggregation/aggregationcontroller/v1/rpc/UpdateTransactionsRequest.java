package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class UpdateTransactionsRequest {
    private List<Transaction> transactions;
    private String user;
    private String credentials;
    private boolean userTriggered;
    private String topic;
    private CredentialsRequestType requestType;
    private String aggregationId;

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getUser() {
        return user;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setUser(String user) {
        this.user = user;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .add("credentials", credentials)
                .add("userTriggered", userTriggered)
                .add("transactions", transactions)
                .toString();
    }

    public CredentialsRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(CredentialsRequestType requestType) {
        this.requestType = requestType;
    }

    public void setRequestTypeFromService(
            se.tink.libraries.credentials.service.CredentialsRequestType
                    serviceCredentialsRequestType) {
        this.requestType =
                CredentialsRequestType.translateFromServiceRequestType(
                        serviceCredentialsRequestType);
    }

    public String getAggregationId() {
        return aggregationId;
    }

    public void setAggregationId(String aggregationId) {
        this.aggregationId = aggregationId;
    }
}
