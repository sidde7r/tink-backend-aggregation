package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RaiffeisenApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage storage;

    public RaiffeisenApiClient(TinkHttpClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
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
                .queryParam(RaiffeisenConstants.QUERY.REDIRECT_URL, RaiffeisenConstants.REDIRECT_URL_VALUE)
                .queryParam(RaiffeisenConstants.QUERY.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        TokenRequest request = new TokenRequest(RaiffeisenConstants.BODY.GRANT_TYPE_CODE,
                RaiffeisenConstants.CLIENT_ID_VALUE, RaiffeisenConstants.CLIENT_SECRET_VALUE, code,
                RaiffeisenConstants.REDIRECT_URL_VALUE);

        return getRequest(RaiffeisenConstants.URL.TOKEN)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData()).toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        return getRequest(RaiffeisenConstants.URL.TOKEN)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshRequest.toTinkRefresh()).toTinkToken();
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

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public TransactionsResponse fetchTransctions(String accountId, Date fromDate, Date toDate) {
        return getAPIRequest(String.format(RaiffeisenConstants.URL.TRANSACTIONS, accountId))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .addBearerToken(getToken())
                .header(RaiffeisenConstants.HEADER.X_IBM_CLIENT_ID, RaiffeisenConstants.CLIENT_ID_VALUE)
                .header(RaiffeisenConstants.HEADER.X_REQUEST_ID, getRequestId())
                .queryParam(RaiffeisenConstants.QUERY.DATE_FROM, formatDate(fromDate))
                .queryParam(RaiffeisenConstants.QUERY.DATE_TO, formatDate(toDate))
                .queryParam(RaiffeisenConstants.QUERY.BOOKING_STATUS, RaiffeisenConstants.QUERY.BOOKING_STATUS_BOTH)
                .get(TransactionsResponse.class);
    }

}
