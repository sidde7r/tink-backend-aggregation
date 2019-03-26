package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.WLFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public final class HVBTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(HVBTransactionFetcher.class);

    private final WLFetcher wlFetcher;

    public HVBTransactionFetcher(
            final WLApiClient client, final HVBStorage storage, final WLConfig config) {
        wlFetcher = new WLFetcher(client, storage, config);
    }

    @Override
    public TransactionKeyPaginatorResponse<Integer> getTransactionsFor(
            final TransactionalAccount account, final Integer key) {
        final TransactionsResponse response =
                wlFetcher.getTransactions(TransactionsResponse.class, account.getAccountNumber());

        response.getErrorMessage()
                .ifPresent(
                        msg -> {
                            logger.error(msg);
                            throw new IllegalStateException(msg);
                        });

        return response;
    }
}
