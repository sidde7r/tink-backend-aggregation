package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AlandsBankenTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AlandsBankenApiClient client;

    public AlandsBankenTransactionalAccountFetcher(AlandsBankenApiClient client) {
        this.client = client;
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

        return accountsResponse.getTransactionalAccounts();
    }
}
