package se.tink.backend.aggregation.agents.framework.wiremock.entities;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import lombok.EqualsAndHashCode;
import se.tink.libraries.pair.Pair;

@EqualsAndHashCode
public class HTTPResponse {

    private final ImmutableSet<Pair<String, String>> responseHeaders;
    private String responseBody;
    private final int statusCode;
    private String toState;
    private String toFault;

    public static class Builder {
        private final ImmutableSet<Pair<String, String>> responseHeaders;
        private final int statusCode;
        private String responseBody;
        private String toState;
        private String toFault;

        public Builder(
                final ImmutableSet<Pair<String, String>> responseHeaders, final int statusCode) {
            this.responseHeaders = responseHeaders;
            this.statusCode = statusCode;
        }

        public Builder setResponseBody(final String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder setToState(final String toState) {
            this.toState = toState;
            return this;
        }

        public Builder setToFault(final String toFault) {
            this.toFault = toFault;
            return this;
        }

        public HTTPResponse build() {
            HTTPResponse response = new HTTPResponse(responseHeaders, statusCode);
            response.responseBody = responseBody;
            response.toState = toState;
            response.toFault = toFault;
            return response;
        }
    }

    private HTTPResponse(
            final ImmutableSet<Pair<String, String>> responseHeaders, final int statusCode) {
        this.responseHeaders = responseHeaders;
        this.statusCode = statusCode;
    }

    public Optional<String> getToState() {
        return Optional.ofNullable(toState);
    }

    public Optional<String> getToFault() {
        return Optional.ofNullable(toFault);
    }

    public Optional<String> getResponseBody() {
        return Optional.ofNullable(responseBody);
    }

    public Set<Pair<String, String>> getResponseHeaders() {
        return responseHeaders;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
