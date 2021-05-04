package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DkbTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final DkbApiClient apiClient;

    public DkbTransactionalAccountFetcher(DkbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .map(this::toTinkAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccountWithBalance(AccountEntity accountEntity) {
        accountEntity.setBalances(getAccountBalance(accountEntity));
        return accountEntity.toTinkAccount();
    }

    private List<BalanceEntity> getAccountBalance(AccountEntity accountEntity) {
        return apiClient.getBalances(accountEntity.getResourceId()).getBalances();
    }
}
