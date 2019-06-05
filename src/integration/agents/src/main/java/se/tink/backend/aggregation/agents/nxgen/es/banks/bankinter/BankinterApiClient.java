package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class BankinterApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public BankinterApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        client.setDebugProxy("http://127.0.0.1:8888");
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public boolean keepAlive() {
        try {
            final HttpResponse response = client.request(Urls.KEEP_ALIVE).get(HttpResponse.class);
            return HttpStatusCodes.isSuccess(response.getStatus());
        } catch (HttpResponseException hre) {
            return false;
        }
    }

    private Cookie convertCookie(org.openqa.selenium.Cookie cookie) {
        BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        newCookie.setDomain(cookie.getDomain());
        newCookie.setPath(cookie.getPath());
        newCookie.setExpiryDate(cookie.getExpiry());
        return newCookie;
    }

    public void storeLoginCookies(Set<org.openqa.selenium.Cookie> cookies) {
        cookies.stream()
                .map(cookie -> convertCookie(cookie))
                .forEach(cookie -> client.addCookie(cookie));
    }
}
