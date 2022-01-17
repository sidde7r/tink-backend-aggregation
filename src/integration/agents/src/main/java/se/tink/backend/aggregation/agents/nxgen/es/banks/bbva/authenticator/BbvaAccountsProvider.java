package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.AccountsProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BbvaAccountsProvider implements AccountsProvider {

    private final AccountFetcher<TransactionalAccount> accountFetcher;
    private final AccountFetcher<CreditCardAccount> creditCardFetcher;

    @Override
    public Collection<? extends Account> getAccounts() {
        return Lists.newArrayList(
                Iterables.concat(
                        accountFetcher.fetchAccounts(), creditCardFetcher.fetchAccounts()));
    }
}
