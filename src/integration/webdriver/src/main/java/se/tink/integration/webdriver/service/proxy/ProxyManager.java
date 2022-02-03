package se.tink.integration.webdriver.service.proxy;

public interface ProxyManager {

    void registerProxyFilter(String key, ProxyFilter proxyFilter);

    void shutDownProxy();
}
