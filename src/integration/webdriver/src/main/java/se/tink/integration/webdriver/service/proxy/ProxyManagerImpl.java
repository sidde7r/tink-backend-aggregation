package se.tink.integration.webdriver.service.proxy;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ProxyManagerImpl implements ProxyManager {

    private final BrowserUpProxy browserUpProxy;
    private final ProxyResponseListener responseListener;

    @Inject
    public ProxyManagerImpl(BrowserUpProxy browserUpProxy) {
        this.browserUpProxy = browserUpProxy;

        this.responseListener = new ProxyResponseListener();
        browserUpProxy.addResponseFilter(responseListener);
    }

    @Override
    public void setProxyResponseMatcher(ProxyResponseMatcher proxyResponseMatcher) {
        responseListener.changeProxyResponseMatcher(proxyResponseMatcher);
    }

    @Override
    public Optional<ResponseFromProxy> waitForMatchingProxyResponse(int waitForSeconds) {
        return responseListener.waitForResponse(waitForSeconds, TimeUnit.SECONDS);
    }

    public void shutDownProxy() {
        browserUpProxy.abort();
    }
}
