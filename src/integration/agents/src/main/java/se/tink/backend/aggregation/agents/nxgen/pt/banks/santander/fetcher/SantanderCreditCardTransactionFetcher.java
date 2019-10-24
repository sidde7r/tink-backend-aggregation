package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SantanderCreditCardTransactionFetcher
        implements TransactionFetcher<CreditCardAccount> {

    private final SantanderApiClient apiClient;

    public SantanderCreditCardTransactionFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return null;
    }
}
