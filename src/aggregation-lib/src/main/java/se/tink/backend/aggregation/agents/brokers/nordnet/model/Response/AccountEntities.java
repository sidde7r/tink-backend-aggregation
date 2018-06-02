package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AccountEntity;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class AccountEntities extends ArrayList<AccountEntity> {
    public List<Account> toAccounts() {
        List<Account> accounts = Lists.newArrayList();

        for (AccountEntity accountEntity : this) {
            if (accountEntity.isSwedishAccount()) {
                accounts.add(accountEntity.toAccount(AccountTypes.INVESTMENT));
            }
        }

        return accounts;
    }
}
