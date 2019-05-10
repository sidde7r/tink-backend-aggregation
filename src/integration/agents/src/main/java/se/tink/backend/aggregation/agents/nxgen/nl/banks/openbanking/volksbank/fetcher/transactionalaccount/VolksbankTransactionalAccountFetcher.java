package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class VolksbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final VolksbankApiClient apiClient;

    public VolksbankTransactionalAccountFetcher(final VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<AccountsEntity> accounts = apiClient.fetchAccounts().getAccounts();

        List<TransactionalAccount> response =
                accounts.stream()
                        .map(
                                account -> {
                                    List<BalanceEntity> balances =
                                            apiClient.readBalance(account).getBalances();
                                    return account.toTinkAccount(balances);
                                })
                        .collect(Collectors.toList());

        return response;
    }
}
