package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CaisseEpragneTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {
    private final CaisseEpargneApiClient apiClient;

    public CaisseEpragneTransactionalAccountFetcher(CaisseEpargneApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse response = apiClient.getAccounts();
        if (!response.isResponseOK()) {
            throw new IllegalStateException(
                    "Error fetching accounts: "
                            + response.getReturnCode()
                            + " - "
                            + response.getReturnDescription());
        }
        response.stream()
                .forEach(
                        account -> {
                            AccountDetailsResponse resp =
                                    apiClient.getAccountDetails(account.getFullAccountNumber());
                            if (resp.isResponseOK()) {
                                account.setIban(resp.getResult().getIban());
                            }
                        });
        return response.stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
