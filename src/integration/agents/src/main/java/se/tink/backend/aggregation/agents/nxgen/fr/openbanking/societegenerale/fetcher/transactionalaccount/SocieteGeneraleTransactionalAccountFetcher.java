package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class SocieteGeneraleTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final SocieteGeneraleApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final EndUserIdentityResponse user = apiClient.getEndUserIdentity();

        return Optional.ofNullable(apiClient.fetchAccounts()).map(AccountsResponse::getCashAccounts)
                .orElseGet(Collections::emptyList).stream()
                .map(accountsItem -> accountsItem.toTinkModel(user.getConnectedPsu()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
