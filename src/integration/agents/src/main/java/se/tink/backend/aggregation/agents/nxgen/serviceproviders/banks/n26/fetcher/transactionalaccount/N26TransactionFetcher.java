package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

public class N26TransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final N26ApiClient n26ApiClient;
    private static final AggregationLogger LOGGER =
            new AggregationLogger(N26TransactionFetcher.class);

    public N26TransactionFetcher(N26ApiClient n26ApiClient) {
        this.n26ApiClient = n26ApiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        try {
            String spaceId = account.getFromTemporaryStorage(N26Constants.SPACE_ID);
            if (Strings.isNullOrEmpty(spaceId)) {
                return n26ApiClient.fetchTransactions(key);
            } else {
                return n26ApiClient.fetchSpaceTransactions(spaceId, key);
            }
        } catch (HttpClientException hce) {
            // N26 times out if you paginate too far back. Stop paginating if it does
            LOGGER.warnExtraLong(
                    "N26 Transaction Pagination error:",
                    N26Constants.Logging.TRANSACTION_PAGINATION_ERROR,
                    hce);
        }
        return new TransactionKeyPaginatorResponseImpl<>();
    }
}
