package se.tink.sa.framework.rest.interceptor;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class EidasHeadersAppender implements ClientHttpRequestInterceptor {

    private static final String EIDAS_CLUSTER_ID_HEADER = "X-Tink-QWAC-ClusterId";
    private static final String EIDAS_APPID_HEADER = "X-Tink-QWAC-AppId";
    private static final String EIDAS_PROXY_REQUESTER = "X-Tink-Debug-ProxyRequester";

    @Value("${security.eidas.proxy.service.clusterId}")
    private String clusterId;

    @Value("${security.eidas.proxy.service.appId}")
    private String appId;

    @Value("${security.eidas.proxy.service.requester}")
    private String requester;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution)
            throws IOException {
        request.getHeaders().add(EIDAS_CLUSTER_ID_HEADER, clusterId);
        request.getHeaders().add(EIDAS_APPID_HEADER, appId);
        request.getHeaders().add(EIDAS_PROXY_REQUESTER, requester);
        return clientHttpRequestExecution.execute(request, body);
    }
}
