package se.tink.integration.webdriver.service.proxy;

public interface ProxyListener {

    void handleRequest(ProxyRequest request);

    void handleResponse(ProxyResponse response);
}
