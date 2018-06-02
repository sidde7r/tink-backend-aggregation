package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class SpankkiTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SpankkiApiClient apiClient;

    public SpankkiTransactionalAccountFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(AccountsEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
