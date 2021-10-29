package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.base;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public abstract class LaBanquePostaleBaseAccountFetcher<T extends Account>
        implements AccountFetcher<T> {

    @Override
    public Collection<T> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts())
                .map(AccountResponse::getAccounts)
                .map(this::mapAccountList)
                .orElseGet(Collections::emptyList);
    }

    private Collection<T> mapAccountList(List<AccountEntity> accountsItemEntities) {
        return accountsItemEntities.stream()
                .filter(this::accountTypeFilterCondition)
                .map(this::map)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    protected final LaBanquePostaleApiClient apiClient;

    protected abstract boolean accountTypeFilterCondition(AccountEntity accountEntity);

    protected abstract Optional<T> map(AccountEntity accountEntity);
}
