package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia;

import java.util.Set;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.HeaderValues;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RuralviaApiClient {

    public final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private @Getter @Setter String globalPositionHtml;
    private @Getter @Setter String headerReferer;

    public RuralviaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        client.setUserAgent(HeaderValues.USER_AGENT);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url);
    }

    public RequestBuilder createBodyFormRequest(URL url, String formToBody) {
        return createRequest(url).body(formToBody, MediaType.APPLICATION_FORM_URLENCODED);
    }

    // TODO
    public boolean keepAlive() {
        return true;
    }

    private Cookie convertCookie(org.openqa.selenium.Cookie cookie) {
        BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        newCookie.setDomain(cookie.getDomain());
        newCookie.setPath(cookie.getPath());
        newCookie.setExpiryDate(cookie.getExpiry());
        return newCookie;
    }

    public void storeLoginCookies(Set<org.openqa.selenium.Cookie> cookies) {
        cookies.stream().map(this::convertCookie).forEach(client::addCookie);
    }

    public String navigateAccountTransactionFirstRequest(URL url, String form) {
        return createBodyFormRequest(url, form).post(String.class);
    }

    public String navigateAccountTransactionsBetweenDates(URL url, String form) {
        return createBodyFormRequest(url, form).post(String.class);
    }

    public String navigateToCreditCardsMovements(URL url) {
        return client.request(url).get(String.class);
    }

    public String navigateToCreditCardTransactionsByDates(URL url, String form) {
        return createBodyFormRequest(url, form).post(String.class);
    }

    public String requestTransactionsBetweenDates(URL url, String params) {
        return createBodyFormRequest(url, params).post(String.class);
    }
}
