package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TargobankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private TargobankApiClient apiClient;

    public TargobankAccountFetcher(TargobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
