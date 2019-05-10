package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AbnAmroApiClient apiClient;

    public TransactionalAccountFetcher(final AbnAmroApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts())
                .map(TransactionalAccountsResponse::getAccounts).orElseGet(Collections::emptyList)
                .stream()
                .map(acc -> acc.toCheckingAccount(apiClient.getBalance(acc.getAccountNumber())))
                .collect(Collectors.toList());
    }
}
