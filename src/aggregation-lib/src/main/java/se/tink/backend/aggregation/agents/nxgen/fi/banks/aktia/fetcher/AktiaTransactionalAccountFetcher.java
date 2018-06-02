package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc.AccountsSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AktiaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AktiaApiClient apiClient;

    private AktiaTransactionalAccountFetcher(AktiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static AktiaTransactionalAccountFetcher create(AktiaApiClient apiClient) {
        return new AktiaTransactionalAccountFetcher(apiClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsSummaryResponse accountsSummaryResponse = apiClient.accountsSummary();
        return accountsSummaryResponse.toTinkAccounts();
    }
}
