package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia;

import java.util.Set;
import javax.ws.rs.core.MediaType;
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
    private String globalPositionHtml;

    public RuralviaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        client.setUserAgent(HeaderValues.USER_AGENT);
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url);
    }

    public RequestBuilder createBodyFormRequest(URL url, String formToBody) {
        return createRequest(url).body(formToBody, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /** */
    public boolean keepAlive() {
        /*try {
            final HttpResponse response = client.request("").get(HttpResponse.class);
            return HttpStatusCodes.isSuccess(response.getStatus());
        } catch (HttpResponseException hre) {
            return false;
        }*/
        return false;
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

    public void setGlobalPositionHtml(String html) {
        this.globalPositionHtml = html;
    }

    public String getGlobalPositionHtml() {
        return globalPositionHtml;
    }

    public String navigateAccountTransactionFirstRequest(RequestBuilder builder) {
        return builder.post(String.class);
    }

    public String navigateAccountTransactionsBetweenDates(RequestBuilder builder) {
        return builder.post(String.class);
    }
}
