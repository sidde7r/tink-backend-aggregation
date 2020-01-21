package se.tink.backend.aggregation.agents.framework;

import java.util.List;
import java.util.Optional;

public class HTTPResponse {

    private final List<String> responseHeaders;
    private final Optional<String> responseBody;
    private final Integer responseCode;

    public HTTPResponse(
            List<String> responseHeaders, Optional<String> responseBody, Integer responseCode) {
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.responseCode = responseCode;
    }

    public List<String> getResponseHeaders() {
        return responseHeaders;
    }

    public Optional<String> getResponseBody() {
        return responseBody;
    }

    public Integer getResponseCode() {
        return responseCode;
    }
}
