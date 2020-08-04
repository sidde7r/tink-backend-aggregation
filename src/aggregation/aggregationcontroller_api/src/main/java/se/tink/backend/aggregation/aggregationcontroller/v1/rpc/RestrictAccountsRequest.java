package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import java.util.List;

/**
 * Request used to pass information on which accounts needs to be removed because they have been
 * restricted due to regulatory restrictions (e.g. PSD2).
 */
public class RestrictAccountsRequest {
    private List<String> accountIds;
    private String credentialsId;
    private String userId;

    private RestrictAccountsRequest() {}

    public static RestrictAccountsRequest of(
            String userId, String credentialsId, List<String> optedOutAccounts) {
        RestrictAccountsRequest restrictAccountsRequest = new RestrictAccountsRequest();
        restrictAccountsRequest.setUserId(userId);
        restrictAccountsRequest.setCredentialsId(credentialsId);
        restrictAccountsRequest.setAccountIds(optedOutAccounts);

        return restrictAccountsRequest;
    }

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
}
