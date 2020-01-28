package se.tink.backend.aggregation.agents.framework.wiremock.entities;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.libraries.pair.Pair;

@EqualsAndHashCode
@Getter
public class HTTPRequest {
    private final String method;
    private final String url;
    private final List<Pair<String, String>> requestHeaders;
    private final String requestBody;

    public HTTPRequest(
            String method,
            String url,
            List<Pair<String, String>> requestHeaders,
            String requestBody) {
        this.method = method;
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
    }

    public HTTPRequest(String method, String url, List<Pair<String, String>> requestHeaders) {
        this.method = method;
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.requestBody = null;
    }

    public Optional<String> getRequestBody() {
        return Optional.ofNullable(requestBody);
    }
}
