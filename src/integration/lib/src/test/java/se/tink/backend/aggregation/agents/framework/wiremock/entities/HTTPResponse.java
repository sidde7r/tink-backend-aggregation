package se.tink.backend.aggregation.agents.framework.wiremock.entities;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import se.tink.libraries.pair.Pair;

@EqualsAndHashCode
public class HTTPResponse {

    private final List<Pair<String, String>> responseHeaders;
    private String responseBody;
    private final int statusCode;
    private String toState;

    public static class Builder {
        private final List<Pair<String, String>> responseHeaders;
        private final int statusCode;
        private String responseBody;
        private String toState;

        public Builder(final List<Pair<String, String>> responseHeaders, final int statusCode) {
            this.responseHeaders = responseHeaders;
            this.statusCode = statusCode;
        }

        public Builder withResponseBody(final String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder withToState(final String toState) {
            this.toState = toState;
            return this;
        }

        public HTTPResponse build() {
            HTTPResponse response = new HTTPResponse(responseHeaders, statusCode);
            response.responseBody = responseBody;
            response.toState = toState;
            return response;
        }
    }

    private HTTPResponse(final List<Pair<String, String>> responseHeaders, final int statusCode) {
        this.responseHeaders = responseHeaders;
        this.statusCode = statusCode;
    }

    public Optional<String> getToState() {
        return Optional.ofNullable(toState);
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
