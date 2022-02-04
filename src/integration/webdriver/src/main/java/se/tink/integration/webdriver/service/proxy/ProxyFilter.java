package se.tink.integration.webdriver.service.proxy;

public interface ProxyFilter {

    void handleRequest(ProxyRequest request);

    void handleResponse(ProxyResponse response);
}
