package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SparebankenSorCreditCardTransactionFetcher implements TransactionFetcher {

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(Account account) {
        return Collections.emptyList();
    }
}
