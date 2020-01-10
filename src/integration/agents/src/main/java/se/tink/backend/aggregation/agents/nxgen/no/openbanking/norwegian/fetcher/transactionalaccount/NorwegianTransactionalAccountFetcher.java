package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NorwegianTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final NorwegianApiClient apiClient;

    public NorwegianTransactionalAccountFetcher(NorwegianApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return Optional.ofNullable(apiClient.fetchAccounts()).map(AccountsResponse::getAccounts)
                .orElseGet(Collections::emptyList).stream()
                .map(acc -> acc.toTinkAccount(apiClient.getBalance(acc.getResourceId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
