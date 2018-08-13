package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard;

import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class EuroInformationCreditCardTransactionsFetcher implements TransactionFetcher<CreditCardAccount> {
    private EuroInformationCreditCardTransactionsFetcher() {
    }

    public static EuroInformationCreditCardTransactionsFetcher create() {
        return new EuroInformationCreditCardTransactionsFetcher();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Lists.emptyList();
    }
}
