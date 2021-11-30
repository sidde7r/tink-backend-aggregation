package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalancesItem;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOnlineTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class SwedbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SwedbankApiClient apiClient;
    private final String market;
    private final PersistentStorage persistentStorage;
    private final AgentComponentProvider componentProvider;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(toTinkAccountWithBalance())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Function<AccountEntity, Optional<TransactionalAccount>> toTinkAccountWithBalance() {
        if (SwedbankConstants.BALTICS.contains(market)) {
            return toTinkAccountWithBalanceBaltics();
        }
        return toTinkAccountWithBalanceSE();
    }

    private Function<AccountEntity, Optional<TransactionalAccount>> toTinkAccountWithBalanceSE() {
        return account -> {
            if (account.getBalances() != null && !account.getBalances().isEmpty()) {
                return account.toTinkAccount(account.getBalances(), market);
            } else {
                return account.toTinkAccount(
                        apiClient.getAccountBalance(account.getResourceId()).getBalances(), market);
            }
        };
    }

    private Function<AccountEntity, Optional<TransactionalAccount>>
            toTinkAccountWithBalanceBaltics() {
        saveHolderName();
        return account -> {
            List<BalancesItem> balance;
            if (account.getBalances() != null && !account.getBalances().isEmpty()) {
                balance = account.getBalances();
            } else {
                balance = apiClient.getAccountBalance(account.getResourceId()).getBalances();
            }
            return account.toBalticTinkAccount(
                    balance, market, persistentStorage.get(StorageKeys.HOLDER_NAME));
        };
    }

    // If we have a saved holder name for a account,
    // we do not have to fetch a new one
    public void saveHolderName() {
        if (isHolderNameNotSaved()) {
            String resourceId =
                    apiClient.fetchAccounts().getAccounts().stream()
                            .findFirst()
                            .get()
                            .getResourceId();

            saveHolderNameToStorage(resourceId);
        }
    }

    private boolean isHolderNameNotSaved() {
        return persistentStorage.get(StorageKeys.HOLDER_NAME) == null
                || persistentStorage.get(StorageKeys.HOLDER_NAME).isEmpty();
    }

    public void saveHolderNameToStorage(String accountId) {
        Optional<FetchOnlineTransactionsResponse> response =
                Optional.ofNullable(
                        apiClient.getOnlineTransactions(
                                accountId,
                                componentProvider
                                        .getLocalDateTimeSource()
                                        .now()
                                        .toLocalDate()
                                        .minusDays(
                                                SwedbankConstants.TimeValues
                                                        .DAYS_TO_FETCH_HOLDER_NAME),
                                componentProvider.getLocalDateTimeSource().now().toLocalDate()));

        if (isOwnerNamePresent(response)) {
            persistentStorage.put(
                    StorageKeys.HOLDER_NAME, response.get().getAccount().getOwnerName());
        }
    }

    private boolean isOwnerNamePresent(Optional<FetchOnlineTransactionsResponse> response) {
        return response.isPresent()
                && response.get().getAccount() != null
                && response.get().getAccount().getOwnerName() != null;
    }
}
