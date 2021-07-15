package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CitadeleTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CitadeleBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<AccountEntity> accounts = apiClient.fetchAccounts().getAccounts();
        storeAccountHolderName(accounts);
        return accounts.stream()
                .map(this::transformAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> transformAccount(AccountEntity accountEntity) {
        List<BalanceEntity> accountBalances = accountEntity.getBalances();
        if (accountBalances == null || accountBalances.isEmpty()) {
            accountBalances = apiClient.fetchBalances(accountEntity).getBalances();
        }
        return accountEntity.toTinkAccount(accountBalances);
    }

    private void storeAccountHolderName(List<AccountEntity> accounts) {
        persistentStorage.put(
                StorageKeys.HOLDER_NAME,
                accounts.stream().map(AccountEntity::getOwnerName).findFirst());
    }
}
