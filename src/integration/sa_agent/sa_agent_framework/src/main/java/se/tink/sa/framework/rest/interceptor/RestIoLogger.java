package se.tink.sa.framework.rest.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

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
            response = logIo(request, body, response);
        }
        return response;
    }

    private ClientHttpResponse logIo(HttpRequest request, byte[] body, ClientHttpResponse response)
            throws IOException {

        if (log.isInfoEnabled()) {
            final ClientHttpResponse responseCopy =
                    new BufferingClientHttpResponseWrapper(response);
            StringBuilder sb =
                    new StringBuilder(
                                    "\n********************************** Http request **********************************")
                            .append("\n")
                            .append("Request:\t")
                            .append(request.getURI())
                            .append("\n")
                            .append("Method:\t")
                            .append(request.getMethod())
                            .append("\n");
            printHeaders(sb, request.getHeaders());

            sb.append("Body:\t")
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
                        .append("\n");
                printHeaders(sb, response.getHeaders());
                //                        @TODO: replace interceptor to copy stream
                sb.append("Response body:\t")
                        .append(
                                StreamUtils.copyToString(
                                        responseCopy.getBody(), Charset.defaultCharset()))
                        .append("\n");
                sb.append(
                        "********************************** Http response end **********************************\n");
                response = responseCopy;
            }
            log.info("{}", sb.toString());
        }

        return response;
    }

    private void printHeaders(StringBuilder sb, HttpHeaders headers) {
        if (CollectionUtils.isNotEmpty(headers.keySet())) {
            sb.append("Headers:\n");
            headers.keySet().stream()
                    .forEach(
                            header ->
                                    sb.append("\t")
                                            .append(header)
                                            .append(":")
                                            .append(headers.get(header))
                                            .append("\n"));
        }
    }
}
