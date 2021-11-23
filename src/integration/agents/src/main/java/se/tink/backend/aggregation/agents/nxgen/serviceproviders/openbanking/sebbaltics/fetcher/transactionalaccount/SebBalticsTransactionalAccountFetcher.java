package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount;

import java.util.Collection;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class SebBalticsTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SebBalticsApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccount(apiClient);
    }
}
