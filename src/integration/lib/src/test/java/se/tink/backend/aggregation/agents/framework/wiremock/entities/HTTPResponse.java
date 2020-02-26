package se.tink.backend.aggregation.agents.framework.wiremock.entities;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import se.tink.libraries.pair.Pair;

@EqualsAndHashCode
public class HTTPResponse {

    private final List<Pair<String, String>> responseHeaders;
    private final String responseBody;
    private final int statusCode;

    public HTTPResponse(
            List<Pair<String, String>> responseHeaders, int statusCode, String responseBody) {
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }

    public HTTPResponse(List<Pair<String, String>> responseHeaders, int statusCode) {
        this.responseHeaders = responseHeaders;
        this.statusCode = statusCode;
        this.responseBody = null;
    }

    public Optional<String> getResponseBody() {
        return Optional.ofNullable(responseBody);
    }

    public List<Pair<String, String>> getResponseHeaders() {
        return responseHeaders;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
