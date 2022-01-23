package se.tink.integration.webdriver.service.proxy;

public interface ProxySaveResponseMatcher {

    boolean matchesResponse(ProxyResponse proxyResponse);
}
