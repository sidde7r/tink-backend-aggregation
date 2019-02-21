package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen;

import java.time.LocalDate;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RaiffeisenApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage storage;
    private final String redirectUrl;

    public RaiffeisenApiClient(TinkHttpClient client, PersistentStorage storage, String redirectUrl) {
        this.client = client;
        this.storage = storage;
        this.redirectUrl = redirectUrl;
    }

    private URL getUrl(String resource) {
        return new URL(RaiffeisenConstants.URL.BASE_AUTH + resource);
    }

    private URL getAPIUrl(String resource) {
        return new URL(RaiffeisenConstants.URL.BASE_API + resource);
    }

    private RequestBuilder getRequest(String resource) {
        return client.request(getUrl(resource));
    }

    private RequestBuilder getAPIRequest(String resource) {
        return client.request(getAPIUrl(resource));
    }

    public URL getAuthorizeUrl(String state) {
        return getRequest(RaiffeisenConstants.URL.OAUTH)
                .header(HttpHeaders.ACCEPT, RaiffeisenConstants.HEADER.ACCEPT_TEXT_HTML)
                .queryParam(RaiffeisenConstants.QUERY.CLIENT_ID, RaiffeisenConstants.CLIENT_ID_VALUE)
                .queryParam(RaiffeisenConstants.QUERY.RESPONSE_TYPE, RaiffeisenConstants.QUERY.RESPONSE_TYPE_CODE)
                .queryParam(RaiffeisenConstants.QUERY.SCOPE, RaiffeisenConstants.QUERY.SCOPE_VALUE)
                .queryParam(RaiffeisenConstants.QUERY.REDIRECT_URL, redirectUrl)
                .queryParam(RaiffeisenConstants.QUERY.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {

        TokenRequest request = new TokenRequest(RaiffeisenConstants.BODY.GRANT_TYPE_CODE,
                RaiffeisenConstants.CLIENT_ID_VALUE, RaiffeisenConstants.CLIENT_SECRET_VALUE, code,
                redirectUrl);

        return getRequest(RaiffeisenConstants.URL.TOKEN)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData()).toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        try {
            RefreshRequest refreshRequest = new RefreshRequest(refreshToken, RaiffeisenConstants.CLIENT_ID_VALUE,
                    RaiffeisenConstants.CLIENT_SECRET_VALUE, redirectUrl);

            return getRequest(RaiffeisenConstants.URL.TOKEN)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .post(TokenResponse.class, refreshRequest.toBody()).toTinkToken();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public void setToken(OAuth2Token token) {
        storage.put(RaiffeisenConstants.STORAGE.TOKEN, token);
    }

    private OAuth2Token getToken() {
        return storage.get(RaiffeisenConstants.STORAGE.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    public AccountsResponse fetchAccounts() {
        return getAPIRequest(RaiffeisenConstants.URL.ACCOUNTS)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .addBearerToken(getToken())
                .header(RaiffeisenConstants.HEADER.X_IBM_CLIENT_ID, RaiffeisenConstants.CLIENT_ID_VALUE)
                .header(RaiffeisenConstants.HEADER.X_REQUEST_ID, getRequestId())
                .queryParam(RaiffeisenConstants.QUERY.WITH_BALANCE, RaiffeisenConstants.QUERY.WITH_BALANCE_TRUE)
                .get(AccountsResponse.class);
    }

    private String formatDate(LocalDate date) {
        return RaiffeisenConstants.DATE.FORMATTER.format(date);
    }

    public TransactionsResponse fetchTransctions(String accountId, LocalDate fromDate, LocalDate toDate, int page) {
        return getAPIRequest(String.format(RaiffeisenConstants.URL.TRANSACTIONS, accountId))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .addBearerToken(getToken())
                .header(RaiffeisenConstants.HEADER.X_IBM_CLIENT_ID, RaiffeisenConstants.CLIENT_ID_VALUE)
                .header(RaiffeisenConstants.HEADER.X_REQUEST_ID, getRequestId())
                .queryParam(RaiffeisenConstants.QUERY.DATE_FROM, formatDate(fromDate))
                .queryParam(RaiffeisenConstants.QUERY.DATE_TO, formatDate(toDate))
                .queryParam(RaiffeisenConstants.QUERY.BOOKING_STATUS, RaiffeisenConstants.QUERY.BOOKING_STATUS_BOTH)
                .queryParam(RaiffeisenConstants.QUERY.PAGE, String.valueOf(page))
                .queryParam(RaiffeisenConstants.QUERY.PAGE_SIZE, "100")
                .get(TransactionsResponse.class);
    }

}
