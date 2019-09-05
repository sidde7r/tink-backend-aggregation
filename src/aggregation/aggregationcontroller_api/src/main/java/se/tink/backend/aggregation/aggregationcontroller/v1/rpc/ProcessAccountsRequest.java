package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import java.util.List;

public class ProcessAccountsRequest {
    private List<String> accountIds;
    private String credentialsId;
    private int credentialsDataVersion;
    private String userId;

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

    public int getCredentialsDataVersion() {
        return credentialsDataVersion;
    }

    public void setCredentialsDataVersion(int credentialsDataVersion) {
        this.credentialsDataVersion = credentialsDataVersion;
    }
}
