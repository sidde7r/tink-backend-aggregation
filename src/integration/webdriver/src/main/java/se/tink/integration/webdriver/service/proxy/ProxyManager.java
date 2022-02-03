package se.tink.integration.webdriver.service.proxy;

public interface ProxyManager {

    void enableRequestsFiltering();

    void enableResponseFiltering();

    void registerProxyFilter(String key, ProxyFilter proxyFilter);

    void shutDownProxy();
}
