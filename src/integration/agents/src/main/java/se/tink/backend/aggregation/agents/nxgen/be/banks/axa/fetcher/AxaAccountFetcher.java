package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public final class AxaAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AxaStorage storage;
    private final AxaApiClient apiClient;

    public AxaAccountFetcher(final AxaApiClient apiClient, final AxaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final int customerId = storage.getCustomerId().orElseThrow(IllegalStateException::new);
        final String accessToken = storage.getAccessToken().orElseThrow(IllegalStateException::new);
        final String locale = storage.getLanguage().orElse(""); // Dutch is the fallback

        final GetAccountsResponse response =
                apiClient.postGetAccounts(customerId, accessToken, locale);

        return response.getAccounts().stream()
                .filter(this::onlySavings)
                .collect(Collectors.toList());
    }

    private boolean onlySavings(TransactionalAccount account) {
        return AccountTypes.SAVINGS == account.getType();
    }
}
