package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, RepositionEntity> {
    private final SantanderEsApiClient apiClient;

    public SantanderEsTransactionFetcher(SantanderEsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<RepositionEntity> getTransactionsFor(
            TransactionalAccount account, RepositionEntity key) {
        String userDataXmlString =
                account.getFromTemporaryStorage(SantanderEsConstants.Storage.USER_DATA_XML);
        String contractIdXmlString =
                account.getFromTemporaryStorage(SantanderEsConstants.Storage.CONTRACT_ID_XML);
        String balanceXmlString =
                account.getFromTemporaryStorage(SantanderEsConstants.Storage.BALANCE_XML);

        String xmlResponseString =
                SerializationUtils.deserializeFromString(
                        apiClient.fetchTransactions(
                                userDataXmlString, contractIdXmlString, balanceXmlString, key),
                        String.class);

        return SantanderEsXmlUtils.parseXmlStringToJson(
                xmlResponseString, TransactionsResponse.class);
    }
}
