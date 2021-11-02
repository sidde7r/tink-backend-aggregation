package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.base;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public abstract class SocieteGeneraleBaseAccountFetcher<T extends Account>
        implements AccountFetcher<T> {
    protected final SocieteGeneraleApiClient apiClient;

    protected abstract boolean accountFilterCondition(AccountsItemEntity accountEntity);

    protected abstract Optional<T> map(AccountsItemEntity accountEntity);

    @Override
    public Collection<T> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts())
                .map(AccountsResponse::getAccounts)
                .map(this::mapAccountList)
                .orElseGet(Collections::emptyList);
    }

    private Collection<T> mapAccountList(List<AccountsItemEntity> accountsItemEntities) {
        return accountsItemEntities.stream()
                .filter(this::accountFilterCondition)
                .map(this::map)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
