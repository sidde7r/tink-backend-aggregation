package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.ApiIdentifier;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class SparebankenVestCreditCardTransactionFetcher
        implements TransactionDatePaginator<CreditCardAccount> {

    private final SparebankenVestApiClient apiClient;

    private SparebankenVestCreditCardTransactionFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestCreditCardTransactionFetcher create(
            SparebankenVestApiClient apiClient) {
        return new SparebankenVestCreditCardTransactionFetcher(apiClient);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        ApiIdentifier apiIdentifier = new ApiIdentifier(account.getApiIdentifier());

        CreditCardTransactionsResponse ccTransactionsResponse =
                apiClient.fetchCreditCardTransactions(apiIdentifier, fromDate, toDate, 0);

        List<CreditCardTransaction> ccTransactions =
                new ArrayList<>(ccTransactionsResponse.getTinkTransactions());

        // ccTransactions are batched within dates, batch(step) size in Constants
        while (ccTransactionsResponse.hasMoreTransactions()) {
            ccTransactionsResponse =
                    apiClient.fetchCreditCardTransactions(
                            apiIdentifier,
                            fromDate,
                            toDate,
                            ccTransactionsResponse.getNextStartOffset());

            ccTransactions.addAll(ccTransactionsResponse.getTinkTransactions());
        }

        return PaginatorResponseImpl.create(ccTransactions);
    }
}
