package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.libraries.jersey.utils.SafelyLoggable;

public class ProcessAccountsRequest implements SafelyLoggable {
    private List<String> accountIds;
    private List<String> requestedAccountIds;
    private String credentialsId;
    private String userId;
    private String operationId;
    private String correlationId;

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("accountIds", accountIds)
                .add("requestedAccountIds", requestedAccountIds)
                .add("credentialsId", credentialsId)
                .add("userId", userId)
                .add("operationId", operationId)
                .add("correlationId", correlationId)
                .toString();
    }

    public List<String> getRequestedAccountIds() {
        return requestedAccountIds;
    }

    public void setRequestedAccountIds(List<String> requestedAccountIds) {
        this.requestedAccountIds = requestedAccountIds;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
