package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaBaseTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, LinkEntity> {
    private static final Logger LOG = LoggerFactory.getLogger(NordeaBaseTransactionFetcher.class);

    private final NordeaBaseApiClient apiClient;
    private final NordeaTransactionParser transactionParser;

    // THIS IS ONLY FOR SANDBOX ENVIRONMENT REMOVE WHEN IN "REAL" ENV
    // when set to a value > 0 will stop after fetching that many times
    // TODO: remove when production, sandbox requires this
    private static final int MAX_TRANSACTION_FETCHES = 0;
    private int numFetches = 0;

    public NordeaBaseTransactionFetcher(
            NordeaBaseApiClient apiClient, NordeaTransactionParser transactionParser) {

        this.apiClient = apiClient;
        this.transactionParser = transactionParser;
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            TransactionalAccount account, LinkEntity key) {
        // first fetch
        if (key == null) {

            // THIS IS ONLY FOR SANDBOX ENVIRONMENT REMOVE WHEN IN "REAL" ENV
            numFetches = 1;

            String transactionPath =
                    account.getFromTemporaryStorage(NordeaBaseConstants.Storage.TRANSACTIONS);

            if (Strings.isNullOrEmpty(transactionPath)) {
                LOG.info("No transactions link found, returning empty");
                return new TransactionsResponse().getPaginatorResponse(transactionParser);
            }

            return apiClient
                    .fetchTransactions(transactionPath)
                    .getPaginatorResponse(transactionParser);
        }

        // THIS IS ONLY FOR SANDBOX ENVIRONMENT REMOVE WHEN IN "REAL" ENV
        if (MAX_TRANSACTION_FETCHES > 0 && numFetches > MAX_TRANSACTION_FETCHES) {
            return new TransactionsResponse().getPaginatorResponse(transactionParser);
        }

        numFetches++;
        return apiClient.fetchTransactions(key.getHref()).getPaginatorResponse(transactionParser);
    }
}
