package se.tink.backend.aggregation.agents.framework;

import java.util.List;
import java.util.Optional;

public class HTTPRequest {
    private final String method;
    private final String url;
    private final List<String> requestHeaders;
    private final Optional<String> requestBody;

    public HTTPRequest(
            String method, String url, List<String> requestHeaders, Optional<String> requestBody) {
        this.method = method;
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getRequestHeaders() {
        return requestHeaders;
    }

    public Optional<String> getRequestBody() {
        return requestBody;
    }
}
