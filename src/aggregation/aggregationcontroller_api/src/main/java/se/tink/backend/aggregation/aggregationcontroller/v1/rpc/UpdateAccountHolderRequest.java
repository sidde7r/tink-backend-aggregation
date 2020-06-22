package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import java.util.Objects;
import se.tink.backend.agents.rpc.AccountHolder;

public class UpdateAccountHolderRequest {
    private AccountHolder accountHolder;
    private String appId;
    private String userId;

    public AccountHolder getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateAccountHolderRequest that = (UpdateAccountHolderRequest) o;
        return Objects.equals(accountHolder, that.accountHolder)
                && Objects.equals(appId, that.appId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountHolder, appId, userId);
    }
}
