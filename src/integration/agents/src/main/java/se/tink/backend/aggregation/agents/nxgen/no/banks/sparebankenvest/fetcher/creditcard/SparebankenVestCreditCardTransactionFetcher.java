package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.BankIdentifier;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class SparebankenVestCreditCardTransactionFetcher  implements TransactionDatePaginator<CreditCardAccount> {

    private final SparebankenVestApiClient apiClient;

    private SparebankenVestCreditCardTransactionFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestCreditCardTransactionFetcher create(SparebankenVestApiClient apiClient) {
        return new SparebankenVestCreditCardTransactionFetcher(apiClient);
    }


    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        List<CreditCardTransaction> transactions = new ArrayList<>();

        BankIdentifier bankIdentifier = new BankIdentifier(account.getBankIdentifier());

        CreditCardTransactionsResponse transactionsResponse =
                apiClient.fetchCreditCardTransactions(bankIdentifier, fromDate, toDate, 0);

        transactions.addAll(transactionsResponse.getTinkTransactions());

        // transactions are batched within dates, batch(step) size in Constants
        while (transactionsResponse.hasMoreTransactions()) {
            transactionsResponse =
                    apiClient.fetchCreditCardTransactions(bankIdentifier, fromDate, toDate, transactionsResponse.getNextStartOffset());

            transactions.addAll(transactionsResponse.getTinkTransactions());
        }

        return PaginatorResponseImpl.create(transactions);
    }
}
