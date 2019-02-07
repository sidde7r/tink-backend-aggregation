package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class IcaBankenCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, Date> {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenTransactionFetcher icaBankenTransactionFetcher;

    public IcaBankenCreditCardFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
        this.icaBankenTransactionFetcher = new IcaBankenTransactionFetcher(apiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        AccountsEntity userAccounts = apiClient.fetchAccounts();

        List<AccountEntity> accountEntities = userAccounts.getAllAccounts();

        return accountEntities.stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<Date> getTransactionsFor(CreditCardAccount account, Date key) {
        return icaBankenTransactionFetcher.fetchTransactions(account, key);
    }
}
