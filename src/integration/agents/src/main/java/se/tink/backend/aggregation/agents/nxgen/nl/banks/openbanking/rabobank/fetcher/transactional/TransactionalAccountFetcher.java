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

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalAccountFetcher.class);
    private final RabobankApiClient apiClient;

    public TransactionalAccountFetcher(final RabobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return Optional.ofNullable(apiClient.fetchAccounts())
                .map(TransactionalAccountsResponse::getAccounts).orElseGet(Collections::emptyList)
                .stream()
                .map(acc -> acc.toCheckingAccount(apiClient.getBalance(acc.getResourceId())))
                .collect(Collectors.toList());
    }
}
