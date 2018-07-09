package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsTransactionFetcher implements TransactionKeyPaginator<TransactionalAccount, RepositionEntity> {
    private final SantanderEsApiClient apiClient;

    public SantanderEsTransactionFetcher(SantanderEsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<RepositionEntity> getTransactionsFor(
            TransactionalAccount account, RepositionEntity key) {
        String userDataXmlString = account.getTemporaryStorage(
                SantanderEsConstants.Storage.USER_DATA_XML, String.class);
        String contractIdXmlString = account.getTemporaryStorage(
                SantanderEsConstants.Storage.CONTRACT_ID_XML, String.class);
        String balanceXmlString = account.getTemporaryStorage(
                SantanderEsConstants.Storage.BALANCE_XML, String.class);

        String xmlResponseString = SerializationUtils.deserializeFromString(
                apiClient.fetchTransactions(userDataXmlString, contractIdXmlString, balanceXmlString, key),
                String.class);

        return SantanderEsXmlUtils.parseXmlStringToJson(xmlResponseString, TransactionsResponse.class);
    }
}
