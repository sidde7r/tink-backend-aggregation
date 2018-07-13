package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class BankdataTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final BankdataApiClient bankClient;

    public BankdataTransactionalAccountFetcher(BankdataApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return bankClient.getAccounts().getTinkAccounts();
    }
}
