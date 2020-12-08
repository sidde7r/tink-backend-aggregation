package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.libraries.jersey.utils.SafelyLoggable;

public class OptOutAccountsRequest implements SafelyLoggable {
    private List<String> accountIds;
    private String credentialsId;
    private String userId;

    private OptOutAccountsRequest() {}

    public static OptOutAccountsRequest of(
            String userId, String credentialsId, List<String> optedOutAccounts) {
        OptOutAccountsRequest optOutAccountsRequest = new OptOutAccountsRequest();
        optOutAccountsRequest.setUserId(userId);
        optOutAccountsRequest.setCredentialsId(credentialsId);
        optOutAccountsRequest.setAccountIds(optedOutAccounts);

        return optOutAccountsRequest;
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

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("accountIds", accountIds)
                .add("credentialsId", credentialsId)
                .add("userId", userId)
                .toString();
    }
}
