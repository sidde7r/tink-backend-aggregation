package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CaixaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {

    private static final int MAX_TRANSACTION_PERIOD = 89;

    private final CaixaApiClient apiClient;

    public CaixaTransactionalAccountFetcher(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().orElseGet(ArrayList::new).stream()
                .peek(
                        account ->
                                account.setBalance(
                                        apiClient
                                                .fetchBalance(account.getFullAccountKey())
                                                .getBalance()))
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        LocalDate from = LocalDate.now().minusDays(MAX_TRANSACTION_PERIOD);
        LocalDate to = LocalDate.now();

        AccountDetailsResponse response =
                apiClient.fetchTransactions(account.getApiIdentifier(), from, to);
        List<Transaction> transactions =
                response.getTransactions().stream()
                        .map(
                                transactionEntity ->
                                        transactionEntity.toTinkTransaction(
                                                response.getAccountCurrency()))
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(transactions, false);
    }
}
