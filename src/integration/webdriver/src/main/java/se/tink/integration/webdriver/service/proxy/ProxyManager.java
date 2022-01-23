package se.tink.integration.webdriver.service.proxy;

public interface ProxyManager {

    void registerProxyListener(String key, ProxyListener proxyListener);

    void shutDownProxy();
}
