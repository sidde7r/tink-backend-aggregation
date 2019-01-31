package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Account;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntities extends ArrayList<AccountEntity> {
    public List<Account> toTinkAccounts() {
        List<Account> accounts = Lists.newArrayList();

        for (AccountEntity account : this) {
            accounts.add(account.toTinkAccount());
        }

        return accounts;
    }

    public AccountEntity find(String accountId) {
        for (AccountEntity account : this) {
            if (Objects.equal(accountId, account.getAccountId())) {
                return account;
            }
        }

        return null;
    }
}
