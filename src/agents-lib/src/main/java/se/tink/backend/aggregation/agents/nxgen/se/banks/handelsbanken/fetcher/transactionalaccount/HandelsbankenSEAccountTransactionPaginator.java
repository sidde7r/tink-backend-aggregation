package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenSEAccountTransactionPaginator
        implements TransactionIndexPaginator<TransactionalAccount> {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSEAccountTransactionPaginator(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account,
            int numberOfTransactions, int startIndex) {

        return sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account))
                .map(handelsbankenAccount ->
                        getTransactionsFor(handelsbankenAccount,
                                numberOfTransactions,
                                startIndex))
                .orElse(null);
    }

    public PaginatorResponse getTransactionsFor(HandelsbankenAccount account,
            int numberOfTransactions, int startIndex) {

        // toTtransactions gives us full url including query.
        // Break it apart and send components to client, substituting the default
        // from/to indices with our pagination values.
        URL url = account.getAccountTransactionsUrl();
        URL baseUrl = url.getUrl();
        String authToken = getAuthTokenFromURL(url);

        return client.transactions(baseUrl, startIndex,
                startIndex + numberOfTransactions, authToken);
    }

    private static String getAuthTokenFromURL(URL url) {

        List<NameValuePair> query = URLEncodedUtils.parse(url.toUri(), "UTF-8");
        Map<String, String> m = query.stream().collect(
                Collectors.toMap(
                        key -> key.getName().toLowerCase(),
                        NameValuePair::getValue));

        return m.get(HandelsbankenSEConstants.QueryParams.AUTH_TOKEN.toLowerCase());
    }
}
