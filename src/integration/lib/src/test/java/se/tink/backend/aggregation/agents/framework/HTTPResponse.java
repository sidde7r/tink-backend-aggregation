package se.tink.backend.aggregation.agents.framework;

import java.util.List;
import java.util.Optional;

public class HTTPResponse {

    private final List<String> responseHeaders;
    private final Optional<String> responseBody;
    private final Integer statusCode;

    public HTTPResponse(
            List<String> responseHeaders, Optional<String> responseBody, Integer statusCode) {
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }

    public List<String> getResponseHeaders() {
        return responseHeaders;
    }

    public Optional<String> getResponseBody() {
        return responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
