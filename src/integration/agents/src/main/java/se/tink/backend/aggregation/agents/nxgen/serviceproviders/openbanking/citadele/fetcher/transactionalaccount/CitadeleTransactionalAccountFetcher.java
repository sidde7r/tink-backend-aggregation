package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CitadeleTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CitadeleBaseApiClient apiClient;

    public CitadeleTransactionalAccountFetcher(CitadeleBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<AccountEntity> accounts = apiClient.fetchAccounts().getAccounts();
        return accounts.stream()
                .map(this::transformAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> transformAccount(AccountEntity accountEntity) {
        List<BalanceEntity> accountBalances = accountEntity.getBalances();
        if (accountBalances == null || accountBalances.isEmpty()) {
            accountEntity.setBalances(apiClient.fetchBalances(accountEntity).getBalances());
        }
        return accountEntity.toTinkAccount();
    }
}
