package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.AccountsItem;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final RabobankApiClient apiClient;

    public TransactionalAccountFetcher(final RabobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final TransactionalAccountsResponse response = apiClient.fetchAccounts();
        return toTransactionalAccounts(response);
    }

    private Collection<TransactionalAccount> toTransactionalAccounts(
            final TransactionalAccountsResponse response) {
        final List<TransactionalAccount> result = new ArrayList<>();
        for (final AccountsItem account : response.getAccounts()) {
            final BalanceResponse balanceResponse = apiClient.getBalance(account.getResourceId());
            result.add(account.toCheckingAccount(balanceResponse));
        }

        return result;
    }
}
