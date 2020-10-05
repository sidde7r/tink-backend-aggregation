package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.common.rpc.SimpleRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities.SavingAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.SavingAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.SavingAccountDetailsTransactionRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BancoPostaSavingTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionPagePaginator {
    private final BancoPostaApiClient apiClient;

    public BancoPostaSavingTransactionalAccountFetcher(BancoPostaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchSavingAccounts(new SimpleRequest()).getSavingAccounts()
                .orElseGet(ArrayList::new).stream()
                .map(this::fetchAccountDetails)
                .map(SavingAccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private SavingAccountEntity fetchAccountDetails(SavingAccountEntity account) {
        SavingAccountDetailsTransactionRequest requestBody =
                new SavingAccountDetailsTransactionRequest(account.getAccountNumber());
        SavingAccountDetailsResponse response = apiClient.fetchSavingAccountDetails(requestBody);
        account.setAccountDetails(response);
        return account;
    }

    @Override
    public PaginatorResponse getTransactionsFor(Account account, int page) {
        SavingAccountDetailsTransactionRequest request =
                new SavingAccountDetailsTransactionRequest(account.getApiIdentifier());
        return apiClient.fetchSavingTransactions(request);
    }
}
