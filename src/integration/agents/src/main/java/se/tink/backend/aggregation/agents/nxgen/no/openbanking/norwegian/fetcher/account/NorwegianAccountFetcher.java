package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class NorwegianAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final NorwegianApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts()).map(AccountsResponse::getAccounts)
                .orElseGet(Collections::emptyList).stream()
                .filter(AccountsItemEntity::isSavings)
                .map(acc -> acc.toTinkAccount(apiClient.getBalance(acc.getResourceId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
