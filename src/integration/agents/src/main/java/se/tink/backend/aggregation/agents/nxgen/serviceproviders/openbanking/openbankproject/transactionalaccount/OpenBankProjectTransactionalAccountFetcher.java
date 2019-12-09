package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class OpenBankProjectTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final OpenBankProjectApiClient apiClient;

    public OpenBankProjectTransactionalAccountFetcher(final OpenBankProjectApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().stream()
                .map(account -> apiClient.fetchAccount(account).toTinkAccount())
                .collect(Collectors.toList());
    }
}
