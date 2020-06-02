package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import com.google.common.base.Strings;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.CustomerInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.PositionsResponse;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class NordnetApiClient {

    private final TinkHttpClient client;
    private String referrer;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public NordnetApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    public boolean isPasswordLogin() {
        return credentials.getType().equals(CredentialsTypes.PASSWORD);
    }

    private RequestBuilder createRequestInSession(String url) {
        RequestBuilder requestBuilder = createBasicRequest(url);

        /* we can use either password authenticator or bankId authenticator. Depending on which one
        used, the request will either use Basic header or bearer token.*/
        if (isPasswordLogin()) {
            String sessionKey = getSessionKeyFromsStorage();
            return requestBuilder.addBasicAuth(sessionKey);
        } else {
            OAuth2Token token = getTokenFromStorage();
            return requestBuilder.addBearerToken(token);
        }
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(NordnetConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Error when retrieving token from storage"));
    }

    private String getSessionKeyFromsStorage() {
        return sessionStorage
                .get(NordnetConstants.StorageKeys.SESSION_KEY, String.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Error when retrieving session key from storage"));
    }

    private RequestBuilder createRequest(String url) {
        return createBasicRequest(url)
                .header(HttpHeaders.CONNECTION, NordnetConstants.HeaderValues.KEEP_ALIVE)
                .header(NordnetConstants.HeaderKeys.REFERRER, referrer)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    public RequestBuilder createBasicRequest(String url) {
        return client.request(url)
                .header(HttpHeaders.USER_AGENT, CommonHeaders.DEFAULT_USER_AGENT)
                .accept(
                        MediaType.APPLICATION_JSON,
                        MediaType.TEXT_HTML,
                        MediaType.APPLICATION_XHTML_XML,
                        NordnetConstants.HeaderKeys.APPLICATION_XML_Q,
                        NordnetConstants.HeaderKeys.GENERIC_MEDIA_TYPE);
    }

    public <T> T get(String loginBankidPageUrl, Class<T> responseClass) {
        return createRequest(loginBankidPageUrl).get(responseClass);
    }

    public <T> T get(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.get(responseClass);
    }

    public <T, R> T post(String url, Class<T> responseClass, R body) {
        return createRequest(url).post(responseClass, body);
    }

    public <T> T post(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.post(responseClass);
    }

    // bankIdAuthenticator
    public void authorizeSession(@NotNull OAuth2Token token) {
        createBasicRequest(NordnetConstants.Urls.ENVIRONMENT_URL)
                .addBearerToken(token)
                .get(HttpResponse.class);
    }

    // passwordAuthenticator
    public void authorizeSession(@NotNull String sessionKey) {
        createBasicRequest(NordnetConstants.Urls.ENVIRONMENT_URL)
                .addBasicAuth(sessionKey)
                .get(HttpResponse.class);
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        RequestBuilder requestBuilder =
                createBasicRequest(NordnetConstants.Urls.GET_CUSTOMER_INFO_URL);
        if (isPasswordLogin()) {
            requestBuilder.addBasicAuth(getSessionKeyFromsStorage());
        }

        IdentityData customerInfo = requestBuilder.get(CustomerInfoResponse.class).toTinkIdentity();
        return new FetchIdentityDataResponse(customerInfo);
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public void setNextReferrer(MultivaluedMap<String, String> headers) {
        String nextReferrer = headers.getFirst(NordnetConstants.HeaderKeys.LOCATION);

        if (!Strings.isNullOrEmpty(nextReferrer)) {
            referrer = NordnetConstants.Urls.BASE_URL + nextReferrer;
        }
    }

    public AccountResponse fetchAccounts() {

        return createRequestInSession(NordnetConstants.Urls.GET_ACCOUNTS_URL)
                .get(AccountResponse.class);
    }

    public AccountInfoResponse fetchAccountInfo(String accountId) {

        return createRequestInSession(
                        String.format(NordnetConstants.Urls.GET_ACCOUNTS_INFO_URL, accountId))
                .get(AccountInfoResponse.class);
    }

    public PositionsResponse getPositions(String accountId) {

        return createRequestInSession(
                        String.format(NordnetConstants.Urls.GET_POSITIONS_URL, accountId))
                .queryParam(NordnetConstants.QueryKeys.INCLUDE_INSTRUMENT_LOAN, "true")
                .get(PositionsResponse.class);
    }
}
