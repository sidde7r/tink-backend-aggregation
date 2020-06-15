package se.tink.backend.agents.rpc;

import java.util.List;
import java.util.Objects;

public class AccountHolder {
    private String accountId;
    private String userId;
    private AccountHolderType type;
    private List<HolderIdentity> identities;

    public AccountHolderType getType() {
        return type;
    }

    public void setType(AccountHolderType type) {
        this.type = type;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<HolderIdentity> getIdentities() {
        return identities;
    }

    public void setIdentities(List<HolderIdentity> identities) {
        this.identities = identities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountHolder that = (AccountHolder) o;
        return Objects.equals(accountId, that.accountId)
                && Objects.equals(userId, that.userId)
                && type == that.type
                && Objects.equals(identities, that.identities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, userId, type, identities);
    }
}
