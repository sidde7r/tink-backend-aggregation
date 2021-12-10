package se.tink.integration.webdriver.service.proxy;

public interface ProxyResponseMatcher {

    boolean matches(ResponseFromProxy responseFromProxy);
}
