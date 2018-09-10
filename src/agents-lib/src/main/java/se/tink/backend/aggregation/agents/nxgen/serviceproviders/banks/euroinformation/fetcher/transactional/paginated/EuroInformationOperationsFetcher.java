package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.OperationSummaryPaginationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.OperationSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class EuroInformationTransactionsFetcher implements // TransactionFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationTransactionsFetcher.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationTransactionsFetcher(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationTransactionsFetcher create(EuroInformationApiClient apiClient) {
        return new EuroInformationTransactionsFetcher(apiClient);
    }

    @Override
    public TransactionKeyPaginatorResponse getTransactionsFor(TransactionalAccount account, String key) {
        //    @Override
        //    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String webId = account.getFromTemporaryStorage(EuroInformationConstants.Tags.WEB_ID);
        //        List<Transaction> transactions = Lists.newArrayList();
        //
        return getOperationsForAccount(webId, key);
        //        operationsForAccount.ifPresent(transactionList ->
        //                transactionList.getOperations().getTransactions().stream()
        //                        .map(OperationEntity::toTransaction)
        //                        .forEach(transactions::add)
        //        );

        //        Optional<TransactionSummaryResponse> transactionsForAccount = getTransactionsForAccount(webId);
        //        transactionsForAccount.ifPresent(transactionList ->
        //                transactionList.getTransactions().stream()
        //                        .map(TransactionEntity::toTransaction)
        //                        .forEach(transactions::add)
        //        );

        //        return transactions;
    }

    private OperationSummaryPaginationResponse getOperationsForAccount(String webId, String key) {
        OperationSummaryResponse operations = apiClient.getOperations(webId, key);
        if (!EuroInformationUtils.isSuccess(operations.getReturnCode())) {
            return null;
        }
        return OperationSummaryPaginationResponse.create(operations);
    }

    private Optional<TransactionSummaryResponse> getTransactionsForAccount(String webId) {
        TransactionSummaryResponse details = apiClient.getTransactions(webId);
        if (!EuroInformationUtils.isSuccess(details.getReturnCode())) {
            return Optional.empty();
        }
        return Optional.of(details);
    }
}
