package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import com.google.common.base.Strings;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.entitiy.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.PositionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class NordnetApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public NordnetApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public <T> T get(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.get(responseClass);
    }

    public <T> T post(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.post(responseClass);
    }

    public RequestBuilder createBasicRequest(URL url) {
        return client.request(url);
    }

    private RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder = createBasicRequest(url);
        return requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, HeaderKeys.BEARER + getAccessToken())
                .header(HttpHeaders.USER_AGENT, HeaderValues.REACT_NATIVE_AGENT);
    }

    public FetchIdentityDataResponse fetchIdentityData() {

        RequestBuilder requestBuilder =
                createRequestInSession(new URL(NordnetConstants.Urls.CUSTOMER_INFO))
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);

        return new FetchIdentityDataResponse(
                get(requestBuilder, CustomerEntity.class).toTinkIdentity());
    }

    public AccountsResponse fetchAccounts() {

        return createRequestInSession(new URL(NordnetConstants.Urls.ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public AccountInfoResponse fetchAccountInfo(String accountId) {

        return createRequestInSession(
                        new URL(Urls.ACCOUNT_INFO)
                                .parameter(NordnetConstants.IdTags.ACCOUNT_ID, accountId))
                .get(AccountInfoResponse.class);
    }

    public PositionsResponse getPositions(String accountId) {

        return createRequestInSession(
                        new URL(NordnetConstants.Urls.POSITIONS)
                                .parameter(NordnetConstants.IdTags.POSITIONS_ID, accountId))
                .queryParam(QueryKeys.INCLUDE_INSTRUMENT_LOAN, QueryValues.TRUE)
                .get(PositionsResponse.class);
    }

    private String getAccessToken() {
        final String accessToken =
                persistentStorage
                        .get(StorageKeys.OAUTH2_TOKEN, TokenResponse.class)
                        .get()
                        .getAccessToken();

        if (Strings.isNullOrEmpty(accessToken)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        return accessToken;
    }
}
