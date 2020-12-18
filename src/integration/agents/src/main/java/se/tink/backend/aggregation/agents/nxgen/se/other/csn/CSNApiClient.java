package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CSNApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public CSNApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public String extractSessionId(HttpResponse httpResponse) {
        return httpResponse.getCookies().stream()
                .filter(
                        cookie ->
                                CSNConstants.Storage.SESSION_ID.equalsIgnoreCase(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(
                        () -> new IllegalStateException("Required value JSESSIONID is missing"));
    }

    public String extractAccessToken() {
        return client.getCookies().stream()
                .filter(
                        cookie ->
                                CSNConstants.Storage.ACCESS_TOKEN.equalsIgnoreCase(
                                        cookie.getName()))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(
                        () -> new IllegalStateException("Required value access_token is missing"));
    }

    public HttpResponse initBankId(MultivaluedMap loginForm) {
        return client.request(CSNConstants.Urls.LOGIN_BANKID)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(HttpResponse.class, loginForm);
    }

    public HttpResponse pollBankId() {
        return client.request(CSNConstants.Urls.BANKID_POLL)
                .accept(MediaType.TEXT_PLAIN)
                .header(CSNConstants.HeaderKeys.REFERER, CSNConstants.Urls.LOGIN_BANKID)
                .header(CSNConstants.HeaderKeys.USER_AGENT, CSNConstants.HeaderValues.USER_AGENT)
                .get(HttpResponse.class);
    }
}
