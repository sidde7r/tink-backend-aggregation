package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import java.net.URI;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RaiffeisenWebApiClient {
    private static final Logger logger = LoggerFactory.getLogger(RaiffeisenWebApiClient.class);
    private final TinkHttpClient client;
    private final Payload payload;

    public RaiffeisenWebApiClient(final TinkHttpClient client, final Provider provider) {
        this.client = client;
        this.payload = SerializationUtils.deserializeFromString(provider.getPayload(), Payload.class);
    }

    private static URL getLastRedirectUrl(final List<URI> redirects) {
        if (redirects.isEmpty()) {
            throw new IllegalStateException("Failed to pickup redirects");
        } else {
            return new URL(redirects.get(redirects.size() - 1).toString());
        }
    }

    private static String getBodyRefreshRegion(Payload payload) {
        return Form.builder()
                .put("loginform", "loginform")
                .put("loginform:LOGINMAND", payload.regionIndex)
                .put("loginform:LOGINVFNR", "")
                .put("javax.faces.ViewState", "e1s1")
                .put("loginform:REFRESHMAND", "loginform:REFRESHMAND")
                .build()
                .serialize();
    }

    private static String getBodyUser(String userName, Payload payload) {
        return Form.builder()
                .put("loginform", "loginform")
                .put("loginform:LOGINMAND", payload.regionIndex)
                .put("loginform:LOGINVFNR", userName)
                .put("javax.faces.ViewState", "e1s2")
                .put("loginform:checkVerfuegereingabe", "loginform:checkVerfuegereingabe")
                .build()
                .serialize();
    }

    private static String getBodyPassword(String encryptedPassword) {
        return Form.builder()
                .put("loginpinform", "loginpinform")
                .put("loginpinform:LOGINPIN", encryptedPassword)
                .put("loginpinform:PIN", "*****")
                .put("javax.faces.ViewState", "e1s2")
                .put("loginpinform:anmeldenPIN", "loginpinform:anmeldenPIN") // Necessary for Tirol account
                .put("loginpinform:anmeldenPINOderCardTAN",
                        "loginpinform:anmeldenPINOderCardTAN") // Necessary for Wien account
                .build()
                .serialize();
    }

    private static String getSelectForm() {
        return Form.builder()
                .put("auswahlseiteForm1", "auswahlseiteForm1")
                .put("javax.faces.ViewState", "e1s3")
                .put("auswahlseiteForm1:j_id209", "auswahlseiteForm1:j_id209")
                .build()
                .serialize();
    }

    private static String getSsoQuery() {
        return Form.builder()
                .put("response_type", "token")
                .put("client_id", "DRB-PFP-RBG")
                .put("scope", "edit")
                .put("redirect_uri", RaiffeisenConstants.Url.REDIRECT.toString())
                .put("state", "eyJzZWNyZXQiOjAuNjI5ODA4MDQwMzUzMDc5LCJoYXNoIjoiIn0=")
                .build()
                .serialize();
    }

    public HttpResponse getHomePage() {
        return client.request(RaiffeisenConstants.Url.HOME).get(HttpResponse.class);
    }

    public HttpResponse RefreshRegion(final HttpResponse homeResponse) {
        final URL url = getLastRedirectUrl(homeResponse.getRedirects());
        return client.request(url)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .body(getBodyRefreshRegion(payload))
                .post(HttpResponse.class);
    }

    public HttpResponse sendUsername(final HttpResponse refreshRegionResponse, final String username) {
        final URL url = getLastRedirectUrl(refreshRegionResponse.getRedirects());
        return client.request(url)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .body(getBodyUser(username, payload))
                .post(HttpResponse.class);
    }

    public HttpResponse sendPassword(final HttpResponse usernameResponse, final String encryptedPassword) {
        final URL url = getLastRedirectUrl(usernameResponse.getRedirects());
        return client.request(url)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .body(getBodyPassword(encryptedPassword))
                .post(HttpResponse.class);
    }

    public HttpResponse sendSelection(final HttpResponse passwordResponse) {
        final URL url = getLastRedirectUrl(passwordResponse.getRedirects());
        return client.request(url)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .body(getSelectForm())
                .post(HttpResponse.class);
    }

    public HttpResponse sendRadSessionId(final HttpResponse selectionResponse) {
        final URL url = getLastRedirectUrl(selectionResponse.getRedirects());
        return client.request(url)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .get(HttpResponse.class);
    }

    public void sendSsoRequest(final URL ssoUrl) {
        client.request(ssoUrl)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .get(HttpResponse.class);
    }

    public URL sso() {
        final URL url = new URL(RaiffeisenConstants.Url.SSO_OAUTH.toString() + "?" + getSsoQuery());
        final HttpResponse r = client.request(url)
                .header(RaiffeisenConstants.Header.CONNECTION_KEY, RaiffeisenConstants.Header.CONNECTION_KEEP_ALIVE)
                .header(RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS_KEY,
                        RaiffeisenConstants.Header.UPGRADE_INSECURE_REQUESTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT,
                        RaiffeisenConstants.Header.ACCEPT_MISC)
                .header(RaiffeisenConstants.Header.REFERER, RaiffeisenConstants.Url.REFERER_SSO.toString())
                .header(HttpHeaders.ACCEPT_ENCODING, RaiffeisenConstants.Header.ACCEPT_ENCODING)
                .header(HttpHeaders.ACCEPT_LANGUAGE, RaiffeisenConstants.Header.ACCEPT_LANGUAGE)
                .get(HttpResponse.class);
        return getLastRedirectUrl(r.getRedirects());
    }

    public AccountResponse getAccountResponse(final WebLoginResponse webLoginResponse) {
        final String r = client.request(RaiffeisenConstants.Url.ACCOUNTS)
                .header(RaiffeisenConstants.Header.CONNECTION_KEY, RaiffeisenConstants.Header.CONNECTION_KEEP_ALIVE)
                .header(HttpHeaders.ACCEPT, RaiffeisenConstants.Header.ACCEPT_ACCOUNTS)
                .header(HttpHeaders.AUTHORIZATION,
                        webLoginResponse.getTokenType() + " " + webLoginResponse.getAccessToken())
                .header(RaiffeisenConstants.Header.REFERER, RaiffeisenConstants.Url.REFERER)
                .header(HttpHeaders.ACCEPT_ENCODING, RaiffeisenConstants.Header.ACCEPT_ENCODING)
                .get(String.class);
        return SerializationUtils.deserializeFromString("{\"accounts\" : " + r + "}", AccountResponse.class);
    }

    public void logOut() {
        try {
            client.request(RaiffeisenConstants.Url.LOGOUT)
                    .header(RaiffeisenConstants.Header.CONNECTION_KEY, RaiffeisenConstants.Header.CONNECTION_KEEP_ALIVE)
                    .header(HttpHeaders.ACCEPT_ENCODING, RaiffeisenConstants.Header.ACCEPT_ENCODING)
                    .header(HttpHeaders.ACCEPT, RaiffeisenConstants.Header.ACCEPT_MISC)
                    .get(HttpResponse.class);
        } catch (HttpResponseException e) {
            logger.warn("Failed to log out", e);
        }
    }

    public void keepAlive(final WebLoginResponse webLoginResponse) {
        client.request(RaiffeisenConstants.Url.KEEP_ALIVE)
                .header(RaiffeisenConstants.Header.CONNECTION_KEY, RaiffeisenConstants.Header.CONNECTION_KEEP_ALIVE)
                .header(HttpHeaders.ACCEPT, RaiffeisenConstants.Header.ACCEPT_ACCOUNTS)
                .header(HttpHeaders.AUTHORIZATION,
                        webLoginResponse.getTokenType() + " " + webLoginResponse.getAccessToken())
                .header(RaiffeisenConstants.Header.REFERER, RaiffeisenConstants.Url.REFERER)
                .header(HttpHeaders.ACCEPT_ENCODING, RaiffeisenConstants.Header.ACCEPT_ENCODING)
                .get(HttpResponse.class);
    }

    public TransactionsResponse getTransactionsResponse(final WebLoginResponse webLoginResponse, final String iban,
            final Date fromDate, final Date toDate) {
        final TransactionsRequest body = new TransactionsRequest(iban, fromDate, toDate);
        HttpResponse r = client.request(RaiffeisenConstants.Url.TRANSACTIONS)
                .header(RaiffeisenConstants.Header.CONNECTION_KEY, RaiffeisenConstants.Header.CONNECTION_KEEP_ALIVE)
                .header(HttpHeaders.ACCEPT, RaiffeisenConstants.Header.ACCEPT_ACCOUNTS)
                .header(HttpHeaders.AUTHORIZATION,
                        webLoginResponse.getTokenType() + " " + webLoginResponse.getAccessToken())
                .header(RaiffeisenConstants.Header.REFERER, RaiffeisenConstants.Url.REFERER)
                .header(HttpHeaders.ACCEPT_ENCODING, RaiffeisenConstants.Header.ACCEPT_ENCODING)
                .body(body)
                .header(HttpHeaders.CONTENT_TYPE, RaiffeisenConstants.Header.APPLICATION_JSON_UTF8)
                .post(HttpResponse.class);
        final String fullResponse = "{\"transactions\":" + r.getBody(String.class) + "}";
        return SerializationUtils.deserializeFromString(fullResponse, TransactionsResponse.class);
    }

    @JsonObject
    private static class Payload {
        private String region;
        private String regionIndex;
        private String regionCode;
    }
}
