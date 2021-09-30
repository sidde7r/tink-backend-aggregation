package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.AccountsItem;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final RabobankApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts())
                .map(TransactionalAccountsResponse::getAccounts).orElseGet(Collections::emptyList)
                .stream()
                .filter(AccountsItem::hasBalancesConsent)
                .map(acc -> acc.toCheckingAccount(apiClient.getBalance(acc.getResourceId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
