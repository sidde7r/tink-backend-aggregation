package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import java.util.Objects;
import se.tink.backend.agents.rpc.AccountHolder;

public class UpdateAccountHolderRequest {
    private AccountHolder accountHolder;

    public AccountHolder getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateAccountHolderRequest that = (UpdateAccountHolderRequest) o;
        return Objects.equals(accountHolder, that.accountHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountHolder);
    }
}
