package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class UnicreditTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditTransactionalAccountFetcher(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        String resourceId = accountEntity.getResourceId();
        return accountEntity.toTinkAccount(
                apiClient.fetchAccountDetails(resourceId).getAccount(),
                apiClient.fetchAccountBalance(resourceId).getBalances());
    }
}
