package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.creditcard;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AsLhvCreditCardTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final AsLhvApiClient apiClient;

    public AsLhvCreditCardTransactionFetcher (AsLhvApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // TODO Remove code duplication (TransactionFetcher)
    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        final GetAccountTransactionsResponse response = apiClient.getAccountTransactions(account.getBankIdentifier(),
                fromDate,
                toDate);
        if (!response.requestSuccessful()) {
            throw new IllegalStateException(String.format("Transaction fetch request failed: %s",
                    response.getErrorMessage()));
        }

        final Collection<? extends Transaction> transactions = response.getTransactions(apiClient.getSessionStorage())
                .stream()
                .filter(transaction -> !transaction.getAmount().isZero())
                .collect(Collectors.toSet());
        return PaginatorResponseImpl.create(transactions);
    }
}
