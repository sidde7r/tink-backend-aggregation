package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Policies;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class IcaBankenCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, LocalDate> {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenTransactionFetcher icaBankenTransactionFetcher;
    private final IcaBankenSessionStorage sessionStorage;

    public IcaBankenCreditCardFetcher(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.icaBankenTransactionFetcher = new IcaBankenTransactionFetcher(apiClient);
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        if (!sessionStorage.hasPolicy(Policies.CARDS)) {
            return Collections.emptyList();
        }

        AccountsEntity userAccounts = apiClient.fetchAccounts();

        List<AccountEntity> accountEntities = userAccounts.getAllAccounts();

        return accountEntities.stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<LocalDate> getTransactionsFor(
            CreditCardAccount account, LocalDate key) {
        return icaBankenTransactionFetcher.fetchTransactions(account, key);
    }
}
