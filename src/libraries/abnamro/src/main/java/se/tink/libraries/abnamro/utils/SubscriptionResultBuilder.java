package se.tink.libraries.abnamro.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.libraries.abnamro.client.model.RejectedContractEntity;
import se.tink.libraries.abnamro.client.model.SubscriptionResult;
import se.tink.libraries.account.rpc.Account;

public class SubscriptionResultBuilder {

    private List<RejectedContractEntity> rejectedContracts;
    private List<Account> accounts;

    public SubscriptionResultBuilder withAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    public SubscriptionResultBuilder withRejectedContracts(List<RejectedContractEntity> rejectedContracts) {
        this.rejectedContracts = rejectedContracts;
        return this;
    }

    public SubscriptionResult build() {

        Preconditions.checkNotNull(rejectedContracts, "Rejected contracts is null");
        Preconditions.checkNotNull(accounts, "Accounts is null");

        ImmutableMap<Long, RejectedContractEntity> rejectedAccountsByAccountNumber = FluentIterable
                .from(rejectedContracts)
                .uniqueIndex(AbnAmroUtils.Functions.REJECTED_CONTRACT_TO_CONTRACT_NUMBER);

        List<Account> subscribedAccounts = Lists.newArrayList();
        List<Account> rejectedAccounts = Lists.newArrayList();

        for (Account account : accounts) {

            Long key = Long.valueOf(account.getBankId());

            if (rejectedAccountsByAccountNumber.containsKey(key)) {
                int rejectedReasonCode = rejectedAccountsByAccountNumber.get(key).getRejectedReasonCode();
                AbnAmroUtils.markAccountAsRejected(account, rejectedReasonCode);
                rejectedAccounts.add(account);
            } else {
                subscribedAccounts.add(account);
            }
        }

        SubscriptionResult result = new SubscriptionResult();
        result.setRejectedAccounts(rejectedAccounts);
        result.setSubscribedAccounts(subscribedAccounts);

        return result;
    }
}
