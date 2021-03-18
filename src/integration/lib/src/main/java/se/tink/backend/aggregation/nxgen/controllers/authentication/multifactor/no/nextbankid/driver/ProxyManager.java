package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.Inject;
import java.util.Optional;
import se.tink.integration.webdriver.utils.Sleeper;

public class ProxyManager {

    private final BrowserUpProxy browserUpProxy;
    private final Sleeper sleeper;

    private final ProxyResponseListener responseListener;

    @Inject
    public ProxyManager(BrowserUpProxy browserUpProxy, Sleeper sleeper) {
        this.browserUpProxy = browserUpProxy;
        this.sleeper = sleeper;

        this.responseListener = new ProxyResponseListener();
        browserUpProxy.addResponseFilter(responseListener);
    }

    public void listenForProxyResponseByResponseUrlSubstring(String responseUrlSubstring) {
        responseListener.listenByResponseUrlSubstring(responseUrlSubstring);
    }

    public Optional<ResponseFromProxy> waitForProxyResponse(int waitForSeconds) {
        for (int i = 0; i < waitForSeconds; i++) {

            Optional<ResponseFromProxy> response = responseListener.getResponseFromProxy();
            if (response.isPresent()) {
                return response;
            }

            sleeper.sleepFor(1_000);
        }
        return Optional.empty();
    }

    public void shutDownProxy() {
        browserUpProxy.abort();
    }
}
