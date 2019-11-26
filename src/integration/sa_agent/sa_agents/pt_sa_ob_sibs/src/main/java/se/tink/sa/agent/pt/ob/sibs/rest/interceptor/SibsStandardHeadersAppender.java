package se.tink.sa.agent.pt.ob.sibs.rest.interceptor;

import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;

public class SibsStandardHeadersAppender implements ClientHttpRequestInterceptor {

    private String clientId;

    public SibsStandardHeadersAppender(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        request.getHeaders()
                .add(
                        SibsConstants.HeaderKeys.TPP_TRANSACTION_ID,
                        UUID.randomUUID().toString().replace("-", ""));
        request.getHeaders()
                .add(
                        SibsConstants.HeaderKeys.TPP_REQUEST_ID,
                        UUID.randomUUID().toString().replace("-", ""));
        request.getHeaders().add(SibsConstants.HeaderKeys.X_IBM_CLIENT_ID, clientId);
        return execution.execute(request, body);
    }
}
