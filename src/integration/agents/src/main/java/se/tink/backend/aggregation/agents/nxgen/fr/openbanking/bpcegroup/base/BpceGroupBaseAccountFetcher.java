package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.base;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.AccountEntity.AccountEntityBuilder;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.AccountEntityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public abstract class BpceGroupBaseAccountFetcher<T extends Account> implements AccountFetcher<T> {

    protected final BpceGroupApiClient apiClient;

    protected abstract boolean accountFilterPredicate(AccountEntity accountEntity);

    protected abstract Optional<T> map(AccountEntity accountEntity, List<BalanceEntity> balances);

    protected abstract Optional<T> map(AccountEntity accountEntity);

    @Override
    public Collection<T> fetchAccounts() {
        final List<AccountEntity> accountEntitiesFirstCallResult = getAccounts(false);

        if (accountEntitiesFirstCallResult.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AccountEntity> accountEntities =
                isConsentCallNeeded(accountEntitiesFirstCallResult)
                        ? recordConsentAndRefetchAccounts(accountEntitiesFirstCallResult)
                        : accountEntitiesFirstCallResult;

        return accountEntities.stream()
                .map(this::mapAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<AccountEntity> recordConsentAndRefetchAccounts(
            List<AccountEntity> accountEntities) {
        recordCustomerConsent(accountEntities);

        return getAccounts(true);
    }

    private List<AccountEntity> getAccounts(boolean forceNewFetch) {
        AccountsResponse accountsResponse =
                forceNewFetch
                        ? apiClient.fetchAccounts()
                        : apiClient.fetchAccountsFromCacheIfPossible();

        return accountsResponse.getAccounts().stream()
                .map(
                        accountEntity ->
                                mapAccountAndAddHolderName(
                                        accountEntity, accountsResponse.getConnectedPsu()))
                .filter(this::accountFilterPredicate)
                .collect(Collectors.toList());
    }

    private Optional<T> mapAccount(AccountEntity accountEntity) {
        if (accountEntity.containsBalances()) {
            return this.map(accountEntity);
        } else {
            final List<BalanceEntity> balances = fetchBalances(accountEntity.getResourceId());
            return this.map(accountEntity, balances);
        }
    }

    private List<BalanceEntity> fetchBalances(String resourceId) {
        return Optional.ofNullable(apiClient.fetchBalances(resourceId))
                .map(BalancesResponse::getBalances)
                .orElseGet(Collections::emptyList);
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

    private AccountEntity mapAccountAndAddHolderName(
            AccountEntityResponse accountEntityResponse, String holderName) {
        AccountEntityBuilder entityBuilder =
                AccountEntity.builder()
                        .cashAccountType(accountEntityResponse.getCashAccountType())
                        .accountId(accountEntityResponse.getAccountId())
                        .resourceId(accountEntityResponse.getResourceId())
                        .product(accountEntityResponse.getProduct())
                        .links(accountEntityResponse.getLinks())
                        .usage(accountEntityResponse.getUsage())
                        .psuStatus(accountEntityResponse.getPsuStatus())
                        .name(accountEntityResponse.getName())
                        .bicFi(accountEntityResponse.getBicFi())
                        .currency(accountEntityResponse.getCurrency())
                        .details(accountEntityResponse.getDetails())
                        .linkedAccount(accountEntityResponse.getLinkedAccount())
                        .balances(accountEntityResponse.getBalances());
        if (!Strings.isNullOrEmpty(accountEntityResponse.getPsuStatus())
                && accountEntityResponse.getPsuStatus().equals("Account Holder")
                && !Strings.isNullOrEmpty(holderName)) {
            entityBuilder = entityBuilder.holderName(holderName);
        }
        return entityBuilder.build();
    }
}
