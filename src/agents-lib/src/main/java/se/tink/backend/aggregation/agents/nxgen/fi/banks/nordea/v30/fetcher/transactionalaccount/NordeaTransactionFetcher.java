package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class NordeaTransactionFetcher implements TransactionIndexPaginator<TransactionalAccount> {

    private final NordeaFiApiClient client;

    public NordeaTransactionFetcher(
            NordeaFiApiClient client) {
        this.client = client;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {

        return client
                .fetchTransactions(startIndex, numberOfTransactions, account.getBankIdentifier(),
                        NordeaFiConstants.Products.ACCOUNT, FetchTransactionsResponse.class);
    }
}
