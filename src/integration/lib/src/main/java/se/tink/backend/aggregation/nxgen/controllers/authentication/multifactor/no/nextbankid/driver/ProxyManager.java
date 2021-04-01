package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ProxyManager {

    private final BrowserUpProxy browserUpProxy;

    private final ProxyResponseListener responseListener;

    @Inject
    public ProxyManager(BrowserUpProxy browserUpProxy) {
        this.browserUpProxy = browserUpProxy;

        this.responseListener = new ProxyResponseListener();
        browserUpProxy.addResponseFilter(responseListener);
    }

    public void setUrlSubstringToListenFor(String responseUrlSubstring) {
        responseListener.changeUrlSubstringToListenFor(responseUrlSubstring);
    }

    public Optional<ResponseFromProxy> waitForProxyResponse(int waitForSeconds) {
        return responseListener.waitForResponse(waitForSeconds, TimeUnit.SECONDS);
    }

    public void shutDownProxy() {
        browserUpProxy.abort();
    }
}
