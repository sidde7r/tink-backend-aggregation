package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class AccountsResponse extends ArrayList<AccountEntity> {

    public Collection<CreditCardAccount> toCreditCardAccounts() {
        return this.stream()
                .filter(AccountEntity::isEnabled)
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}
