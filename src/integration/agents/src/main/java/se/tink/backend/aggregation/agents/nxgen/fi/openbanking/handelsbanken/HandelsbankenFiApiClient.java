package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.rpc.FiTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenFiApiClient extends HandelsbankenBaseApiClient {

    public HandelsbankenFiApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, String market) {
        super(client, persistentStorage, market);
    }

    @Override
    public FiTransactionResponse getTransactions(String accountId, Date dateFrom, Date dateTo) {
        RequestBuilder request =
                createRequest(Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParams.ACCOUNT_ID, accountId))
                        .queryParam(
                                QueryKeys.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                        .queryParam(
                                QueryKeys.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo));

        return requestRefreshableGet(request, FiTransactionResponse.class);
    }
}
