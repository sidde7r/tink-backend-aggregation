package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import java.util.Collection;
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
                .collect(Collectors.toList());
    }

    public TransactionalAccount toTinkAccount(AccountEntity accountEntity) {
        return accountEntity.toTinkAccount(
                apiClient.fetchAccountBalance(accountEntity.getResourceId()).getBalance());
    }
}
