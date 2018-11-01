package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AsLhvTransactionFetcher {
    private final AsLhvSessionStorage sessionStorage;
    private final AsLhvApiClient apiClient;

    public AsLhvTransactionFetcher(final AsLhvApiClient apiClient, final AsLhvSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public <T extends Account> PaginatorResponse getTransactionsFor(
            final T account,
            final Date fromDate,
            final Date toDate) {
        final GetAccountTransactionsResponse response =
                apiClient.getAccountTransactions(account.getBankIdentifier(), fromDate, toDate);
        if (!response.requestSuccessful()) {
            throw new IllegalStateException(String.format("Transaction fetch request failed: %s",
                    response.getErrorMessage()));
        }

        final Collection<? extends Transaction> transactions = response.getTransactions(sessionStorage)
                .stream()
                .filter(transaction -> !transaction.getAmount().isZero())
                .collect(Collectors.toSet());
        return PaginatorResponseImpl.create(transactions);
    }
}
