package se.tink.integration.webdriver.service.proxy;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.Inject;

public class ProxyManagerImpl implements ProxyManager {

    private final BrowserUpProxy browserUpProxy;
    private final ProxyFilterRegistry proxyFilterRegistry;

    @Inject
    public ProxyManagerImpl(
            BrowserUpProxy browserUpProxy, ProxyFilterRegistry proxyFilterRegistry) {
        this.browserUpProxy = browserUpProxy;
        this.proxyFilterRegistry = proxyFilterRegistry;
    }

    @Override
    public void enableRequestsFiltering() {
        browserUpProxy.addRequestFilter(proxyFilterRegistry);
    }

    @Override
    public void enableResponseFiltering() {
        browserUpProxy.addResponseFilter(proxyFilterRegistry);
    }

    @Override
    public void registerProxyFilter(String key, ProxyFilter proxyFilter) {
        proxyFilterRegistry.registerProxy(key, proxyFilter);
    }

    public void shutDownProxy() {
        browserUpProxy.stop();
    }
}
