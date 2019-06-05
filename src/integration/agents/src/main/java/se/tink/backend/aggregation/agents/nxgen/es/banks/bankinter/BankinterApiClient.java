package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Set;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class BankinterApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public BankinterApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        client.setDebugProxy("http://127.0.0.1:8888");
        this.persistentStorage = persistentStorage;
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

    public IdentityDataResponse fetchIdentityData() {
        // response has content-type text/html, but is actually JSON
        final HttpResponse response = client.request(Urls.IDENTITY_DATA).get(HttpResponse.class);
        final String responseBody = response.getBody(String.class);
        return SerializationUtils.deserializeFromString(responseBody, IdentityDataResponse.class);
    }
}
