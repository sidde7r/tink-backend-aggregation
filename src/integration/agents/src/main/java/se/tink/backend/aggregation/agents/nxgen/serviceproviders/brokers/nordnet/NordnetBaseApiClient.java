package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.PositionsResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public abstract class NordnetBaseApiClient {

    private final TinkHttpClient client;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public NordnetBaseApiClient(
            TinkHttpClient client,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public <T> T get(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.get(responseClass);
    }

    public <T> T post(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.post(responseClass);
    }

    public boolean isPasswordLogin() {
        return credentials.getType().equals(CredentialsTypes.PASSWORD);
    }

    public RequestBuilder createBasicRequest(URL url) {
        return client.request(url);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder = createBasicRequest(url);
        return requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                .header(HttpHeaders.USER_AGENT, HeaderValues.REACT_NATIVE_AGENT);
    }

    public void authorizeSession() {

        try {
            final String requestBody =
                    new JSONObject()
                            .put(HttpHeaders.USER_AGENT, HeaderValues.REACT_NATIVE_AGENT)
                            .toString();

            createBasicRequest(new URL(Urls.INIT_LOGIN))
                    .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                    .type(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.USER_AGENT, HeaderValues.NORDNET_AGENT)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(HeaderValues.REACT_NATIVE_AGENT)
                    .put(HttpResponse.class, requestBody);

        } catch (JSONException e) {
            throw new IllegalStateException("JSON object creation error");
        }
    }

    public abstract FetchIdentityDataResponse fetchIdentityData();

    public AccountsResponse fetchAccounts() {

        return createRequestInSession(new URL(NordnetBaseConstants.Urls.ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public AccountInfoResponse fetchAccountInfo(String accountId) {

        return createRequestInSession(
                        new URL(Urls.ACCOUNT_INFO)
                                .parameter(NordnetBaseConstants.IdTags.ACCOUNT_ID, accountId))
                .get(AccountInfoResponse.class);
    }

    public PositionsResponse getPositions(String accountId) {

        return createRequestInSession(
                        new URL(NordnetBaseConstants.Urls.POSITIONS)
                                .parameter(NordnetBaseConstants.IdTags.POSITIONS_ID, accountId))
                .queryParam(QueryKeys.INCLUDE_INSTRUMENT_LOAN, QueryValues.TRUE)
                .get(PositionsResponse.class);
    }

    private String getAuthorizationHeader() {

        if (isPasswordLogin()) {
            final String sessionKey =
                    sessionStorage
                            .get(StorageKeys.SESSION_KEY, String.class)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            return HeaderKeys.BASIC + EncodingUtils.encodeAsBase64String(sessionKey);
        } else {
            return HeaderKeys.BEARER
                    + persistentStorage
                            .get(StorageKeys.OAUTH2_TOKEN, TokenResponse.class)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception)
                            .getAccessToken();
        }
    }
}
