package se.tink.sa.framework.rest.interceptor;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class RestIoLogger implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = null;
        try {
            response = execution.execute(request, body);
        } catch (RuntimeException ex) {
            throw ex;
        } finally {
            logIo(request, body, response);
        }
        return response;
    }

    private void logIo(HttpRequest request, byte[] body, ClientHttpResponse response)
            throws IOException {

        if (log.isInfoEnabled()) {
            StringBuilder sb =
                    new StringBuilder(
                                    "\n********************************** Http request **********************************")
                            .append("\n")
                            .append("Request:\t")
                            .append(request.getURI())
                            .append("\n")
                            .append("Method:\t")
                            .append(request.getMethod())
                            .append("\n")
                            .append("Headers:\t")
                            .append(request.getHeaders())
                            .append("\n")
                            .append("Body:\t")
                            .append(new String(body))
                            .append("\n")
                            .append(
                                    "********************************** Http request end ********************************** \n");

            if (response != null) {
                sb.append(
                                "********************************** Http response **********************************\n")
                        .append("Status code:\t")
                        .append(response.getStatusCode())
                        .append("\n")
                        .append("Status text:\t")
                        .append(response.getStatusText())
                        .append("\n")
                        .append("Headers:\t")
                        .append(response.getHeaders())
                        .append("\n")
                        //                        @TODO: replace interceptor to copy stream
                        //                        .append("Response
                        // body:\t").append(StreamUtils.copyToString(response.getBody(),
                        // Charset.defaultCharset())).append("\n")
                        .append(
                                "********************************** Http response end **********************************\n");
            }
            log.info("{}", sb.toString());
        }
    }
}
