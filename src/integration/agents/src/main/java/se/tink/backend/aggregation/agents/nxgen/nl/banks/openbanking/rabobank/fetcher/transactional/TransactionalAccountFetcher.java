package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final RabobankApiClient apiClient;

    private static Logger logger = LoggerFactory.getLogger(TransactionalAccountFetcher.class);

    public TransactionalAccountFetcher(final RabobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private Collection<TransactionalAccount> doFetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts())
                .map(TransactionalAccountsResponse::getAccounts).orElseGet(Collections::emptyList)
                .stream()
                .map(acc -> acc.toCheckingAccount(apiClient.getBalance(acc.getResourceId())))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // TODO Revert this fallback mechanism after MIYAG-787 is closed
        try {
            return doFetchAccounts();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                if (e.getResponse().getBody(String.class).contains("SIGNATURE_INVALID")) {
                    logger.info("MIYAG-787 certificate fallback triggered");
                    apiClient.switchToOldCertificate();
                    return doFetchAccounts();
                }
            }
            throw e;
        }
    }
}
