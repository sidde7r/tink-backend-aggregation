package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalancesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final SessionStorage sessionStorage;

    public HandelsbankenBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_IBM_CLIENT_ID, persistentStorage.get(StorageKeys.CLIENT_ID))
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.ACCESS_TOKEN))
                .header(
                        HeaderKeys.TPP_TRANSACTION_ID,
                        persistentStorage.get(StorageKeys.TPP_TRANSACTION_ID))
                .header(
                        HeaderKeys.TPP_REQUEST_ID,
                        persistentStorage.get(StorageKeys.TPP_REQUEST_ID))
                .header(
                        HeaderKeys.PSU_IP_ADDRESS,
                        persistentStorage.get(StorageKeys.PSU_IP_ADDRESS))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    public <T> T getAccountList(Class<T> returnType) {
        return createRequest(new URL(Urls.BASE_URL + Urls.ACCOUNTS)).get(returnType);
    }

    public BalancesEntity getAccountDetails(String accountId) {
        return createRequest(
                        new URL(Urls.BASE_URL + String.format(Urls.ACCOUNT_DETAILS, accountId)))
                .queryParam(QueryKeys.WITH_BALANCE, Boolean.TRUE.toString())
                .get(BalancesEntity.class);
    }

    public TransactionsResponse getTransactions(String accountId, Date dateFrom, Date dateTo) {
        return createRequest(
                        new URL(
                                Urls.BASE_URL
                                        + String.format(Urls.ACCOUNT_TRANSACTIONS, accountId)))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo))
                .get(TransactionsResponse.class);
    }
}
