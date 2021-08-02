package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.SantanderEsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, RepositionEntity> {
    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public SantanderEsTransactionFetcher(
            SantanderEsApiClient apiClient, SantanderEsSessionStorage santanderEsSessionStorage) {
        this.apiClient = apiClient;
        this.santanderEsSessionStorage = santanderEsSessionStorage;
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

        String response = "";
        try {
            response =
                    apiClient.fetchTransactions(
                            userDataXmlString, contractIdXmlString, balanceXmlString, key);
        } catch (HttpResponseException e) {
            SantanderEsAuthenticator santanderEsAuthenticator =
                    new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage);
            santanderEsAuthenticator.authenticate(
                    santanderEsSessionStorage.getUserId(), santanderEsSessionStorage.getPassword());
            response =
                    apiClient.fetchTransactions(
                            userDataXmlString, contractIdXmlString, balanceXmlString, key);
        }

        String xmlResponseString = SerializationUtils.deserializeFromString(response, String.class);

        return SantanderEsXmlUtils.parseXmlStringToJson(
                xmlResponseString, TransactionsResponse.class);
    }
}
