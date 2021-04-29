package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class WizinkTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private WizinkApiClient wizinkApiClient;

    public WizinkTransactionFetcher(WizinkApiClient wizinkApiClient) {
        this.wizinkApiClient = wizinkApiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String internalKey = account.getApiIdentifier();
        return wizinkApiClient
                .fetchTransactionsFrom90Days(internalKey)
                .getTransactionResponse()
                .getTransactions();
    }
}
