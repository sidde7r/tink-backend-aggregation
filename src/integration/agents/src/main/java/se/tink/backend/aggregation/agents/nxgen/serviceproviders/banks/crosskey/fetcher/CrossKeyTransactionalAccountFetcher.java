package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CrossKeyTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final CrossKeyApiClient client;
    private final CrossKeyConfiguration agentConfiguration;

    public CrossKeyTransactionalAccountFetcher(
            CrossKeyApiClient client, CrossKeyConfiguration agentConfiguration) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse = client.fetchAccounts();

        if (accountsResponse.isFailure()) {
            // When Ålandsbanken are having problems they return sometimes don't return the
            // expected response on some requests. Instead the return errors like "NOT_AUTHORIZED"
            // and "DEVICE_BLOCKED".
            throw new IllegalStateException("Was not able to fetch accounts.");
        }

        return accountsResponse.getTransactionalAccounts(agentConfiguration);
    }
}
