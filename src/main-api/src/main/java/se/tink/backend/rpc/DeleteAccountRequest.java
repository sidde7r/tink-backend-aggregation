package se.tink.backend.rpc;

public class DeleteAccountRequest {

    private String userId;
    private String credentialsId;
    private String accountId;

    public String getUserId() {
        return userId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
