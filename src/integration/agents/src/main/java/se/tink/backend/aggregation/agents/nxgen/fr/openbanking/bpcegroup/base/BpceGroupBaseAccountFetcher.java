package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.base;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public abstract class BpceGroupBaseAccountFetcher<T extends Account> implements AccountFetcher<T> {

    protected final BpceGroupApiClient apiClient;

    protected abstract boolean accountFilterPredicate(AccountEntity accountEntity);

    protected abstract Optional<T> map(AccountEntity accountEntity, List<BalanceEntity> balances);

    @Override
    public Collection<T> fetchAccounts() {
        final List<AccountEntity> accountEntitiesFirstCallResult = getAccounts();

        if (accountEntitiesFirstCallResult.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AccountEntity> accountEntities =
                isConsentCallNeeded(accountEntitiesFirstCallResult)
                        ? recordConsentAndRefetchAccounts(accountEntitiesFirstCallResult)
                        : accountEntitiesFirstCallResult;

        return accountEntities.stream()
                .map(
                        account -> {
                            final List<BalanceEntity> balances =
                                    getBalances(account.getResourceId());
                            return this.map(account, balances);
                        })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<AccountEntity> getAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(this::accountFilterPredicate)
                .collect(Collectors.toList());
    }

    protected List<BalanceEntity> getBalances(String resourceId) {
        return Optional.ofNullable(apiClient.fetchBalances(resourceId))
                .map(BalancesResponse::getBalances)
                .orElseGet(Collections::emptyList);
    }

    private List<AccountEntity> recordConsentAndRefetchAccounts(
            List<AccountEntity> accountEntities) {
        recordCustomerConsent(accountEntities);

        return getAccounts();
    }

    private void recordCustomerConsent(List<AccountEntity> accountEntities) {
        final List<String> accountIds =
                accountEntities.stream().map(AccountEntity::getIban).collect(Collectors.toList());

        apiClient.recordCustomerConsent(accountIds);
    }

    private static boolean isConsentCallNeeded(List<AccountEntity> accountEntities) {
        return accountEntities.stream()
                .anyMatch(BpceGroupBaseAccountFetcher::doesAccountLackConsentedData);
    }

    private static boolean doesAccountLackConsentedData(AccountEntity accountEntity) {
        return Objects.isNull(accountEntity.getResourceId())
                || Objects.isNull(accountEntity.getLinks())
                || Objects.isNull(accountEntity.getLinks().getBalances())
                || Objects.isNull(accountEntity.getLinks().getTransactions());
    }
}
