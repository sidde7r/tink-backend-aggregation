package se.tink.integration.webdriver.service.proxy;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.Inject;

public class ProxyManagerImpl implements ProxyManager {

    private final BrowserUpProxy browserUpProxy;
    private final ProxyFilter proxyFilter;

    @Inject
    public ProxyManagerImpl(BrowserUpProxy browserUpProxy, ProxyFilter proxyFilter) {
        this.browserUpProxy = browserUpProxy;
        this.proxyFilter = proxyFilter;

        browserUpProxy.addRequestFilter(proxyFilter);
        browserUpProxy.addResponseFilter(proxyFilter);
    }

    @Override
    public void registerProxyListener(String key, ProxyListener proxyListener) {
        proxyFilter.addListener(key, proxyListener);
    }

    public void shutDownProxy() {
        browserUpProxy.stop();
    }
}
