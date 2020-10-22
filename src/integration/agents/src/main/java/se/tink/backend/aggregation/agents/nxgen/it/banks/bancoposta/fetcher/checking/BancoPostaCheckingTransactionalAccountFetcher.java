package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.TransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BancoPostaCheckingTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionPagePaginator {

    private final BancoPostaApiClient apiClient;

    public BancoPostaCheckingTransactionalAccountFetcher(BancoPostaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().orElseGet(ArrayList::new).stream()
                .map(this::fetchAccountDetails)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private AccountEntity fetchAccountDetails(AccountEntity account) {
        AccountDetailsRequest requestBody =
                new AccountDetailsRequest(account.getAccountIdentifier());
        AccountDetailsResponse accountDetails = apiClient.fetchAccountDetails(requestBody);
        account.setAccountDetails(accountDetails);
        return account;
    }

    @Override
    public PaginatorResponse getTransactionsFor(Account account, int page) {
        TransactionsRequest request = new TransactionsRequest(account.getApiIdentifier(), 40, page);
        String currency = account.getExactBalance().getCurrencyCode();
        return apiClient.fetchTransactions(request).enrichWithAccountCurrency(currency);
    }
}
