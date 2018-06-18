package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class TargoBankCreditCardTransactionsFetcher implements TransactionFetcher<CreditCardAccount> {
    private TargoBankCreditCardTransactionsFetcher() {
    }

    public static TargoBankCreditCardTransactionsFetcher create() {
        return new TargoBankCreditCardTransactionsFetcher();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Lists.emptyList();
    }
}
