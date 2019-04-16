package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class EnterCardTransactionFetcher implements TransactionPagePaginator<CreditCardAccount> {

    private EnterCardApiClient apiClient;

    public EnterCardTransactionFetcher(EnterCardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        TransactionResponse transactionResponse =
                apiClient.fetchTransactions(account.getAccountNumber(), page, 50);

        List<Transaction> transactions =
                transactionResponse.getTransactions().stream()
                        .map(
                                se.tink.backend.aggregation.agents.nxgen.serviceproviders
                                                .creditcards.entercard.fetcher.rpc.Transaction
                                        ::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions, canFetchMore(transactionResponse));
    }

    private boolean canFetchMore(TransactionResponse response) {
        return response.getPagination().canFetchMore();
    }
}
