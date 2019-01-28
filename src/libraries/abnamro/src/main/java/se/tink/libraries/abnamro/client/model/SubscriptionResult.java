package se.tink.libraries.abnamro.client.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.account.rpc.Account;

public class SubscriptionResult {
    private ImmutableList<Account> subscribedAccounts = ImmutableList.of();
    private ImmutableList<Account> rejectedAccounts = ImmutableList.of();

    public void setSubscribedAccounts(List<Account> subscribedAccounts) {
        this.subscribedAccounts = ImmutableList.copyOf(subscribedAccounts);
    }

    public ImmutableList<Account> getSubscribedAccounts() {
        return subscribedAccounts;
    }

    public void setRejectedAccounts(List<Account> rejectedAccounts) {
        this.rejectedAccounts = ImmutableList.copyOf(rejectedAccounts);
    }

    public ImmutableList<Account> getRejectedAccounts() {
        return rejectedAccounts;
    }
}
