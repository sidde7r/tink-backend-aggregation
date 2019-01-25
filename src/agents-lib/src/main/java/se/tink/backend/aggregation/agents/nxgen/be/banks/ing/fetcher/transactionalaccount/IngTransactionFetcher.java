package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.PendingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.PendingPaymentsResponseEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.agents.rpc.Credentials;

public class IngTransactionFetcher implements TransactionPagePaginator<TransactionalAccount>,
        UpcomingTransactionFetcher<TransactionalAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(IngTransactionFetcher.class);

    private final Credentials credentials;
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;

    public IngTransactionFetcher(Credentials credentials, IngApiClient apiClient, IngHelper ingHelper) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.ingHelper = ingHelper;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        String transactionsUrl = ingHelper.getUrl(IngConstants.RequestNames.GET_TRANSACTIONS);

        return apiClient.getTransactions(
                transactionsUrl,
                account.getBankIdentifier(),
                getStartIndex(page),
                getEndIndex(page));
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        PendingPaymentsResponseEntity pendingPaymentsResponseEntity = ingHelper.retrieveLoginResponse()
                .map(loginResponse -> apiClient.getPendingPayments(loginResponse, account.getBankIdentifier()))
                .orElseThrow(() -> new IllegalStateException("Login response not found."));

        return pendingPaymentsResponseEntity.getPendingPayments().stream()
                .map(PendingPaymentEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }

    private int getStartIndex(int page) {
        return (page * IngConstants.Fetcher.MAX_TRANSACTIONS_IN_BATCH) + 1;
    }

    private int getEndIndex(int page) {
        return (page + 1) * IngConstants.Fetcher.MAX_TRANSACTIONS_IN_BATCH;
    }
}
