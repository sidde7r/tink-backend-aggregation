package se.tink.integration.webdriver.service.proxy;

import java.util.Optional;

public interface ProxyManager {

    void setProxyResponseMatcher(ProxyResponseMatcher proxyResponseMatcher);

    Optional<ResponseFromProxy> waitForMatchingProxyResponse(int waitForSeconds);

    void shutDownProxy();
}
