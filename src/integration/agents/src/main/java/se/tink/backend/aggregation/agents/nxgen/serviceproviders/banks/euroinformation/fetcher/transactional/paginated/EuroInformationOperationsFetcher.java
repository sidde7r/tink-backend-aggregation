package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.paginated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.paginated.OperationSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationOperationsFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EuroInformationOperationsFetcher.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationOperationsFetcher(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationOperationsFetcher create(EuroInformationApiClient apiClient) {
        return new EuroInformationOperationsFetcher(apiClient);
    }

    @Override
    public TransactionKeyPaginatorResponse getTransactionsFor(
            TransactionalAccount account, String key) {
        String webId = account.getFromTemporaryStorage(EuroInformationConstants.Tags.WEB_ID);
        return getOperationsForAccount(webId, key);
    }

    private OperationSummaryResponse getOperationsForAccount(String webId, String key) {
        OperationSummaryResponse operations = apiClient.getTransactionsWithPfm(webId, key);
        String returnCode = operations.getReturnCode();
        if (!EuroInformationUtils.isSuccess(operations.getReturnCode())) {
            LOGGER.info(
                    EuroInformationErrorCodes.getByCodeNumber(returnCode).toString()
                            + SerializationUtils.serializeToString(operations));
        }
        return operations;
    }
}
