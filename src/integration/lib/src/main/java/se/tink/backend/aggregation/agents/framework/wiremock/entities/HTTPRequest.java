package se.tink.backend.aggregation.agents.framework.wiremock.entities;

import com.google.common.collect.ImmutableSet;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import se.tink.libraries.pair.Pair;

@EqualsAndHashCode
public class HTTPRequest {
    private final String method;
    private final String path;
    private final ImmutableSet<NameValuePair> query;
    private final ImmutableSet<Pair<String, String>> requestHeaders;
    private String requestBody;
    private String expectedState;

    public static class Builder {
        private final String method;
        private final String url;
        private final ImmutableSet<Pair<String, String>> requestHeaders;
        private String requestBody;
        private String expectedState;

        public Builder(
                final String method,
                final String url,
                final ImmutableSet<Pair<String, String>> requestHeaders) {
            this.method = method;
            this.url = url;
            this.requestHeaders = requestHeaders;
        }

        public Builder setRequestBody(final String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder setExpectedState(final String expectedState) {
            this.expectedState = expectedState;
            return this;
        }

        public HTTPRequest build() {
            HTTPRequest request = new HTTPRequest(method, url, requestHeaders);
            request.requestBody = requestBody;
            request.expectedState = expectedState;
            return request;
        }
    }

    private HTTPRequest(
            final String method,
            final String url,
            final ImmutableSet<Pair<String, String>> requestHeaders) {

        URI uri = URI.create(url);

        this.path = uri.getRawPath();
        this.query = ImmutableSet.copyOf(URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.name()));

        this.method = method;
        this.requestHeaders = requestHeaders;
    }

    public Optional<String> getExpectedState() {
        return Optional.ofNullable(expectedState);
    }

    private NameValuePair urlEncode(final NameValuePair pair) {
        try {
            return new BasicNameValuePair(
                    URLEncoder.encode(pair.getName(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(pair.getValue(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public ImmutableSet<NameValuePair> getQuery() {
        return query;
    }

    public Optional<String> getRequestBody() {
        return Optional.ofNullable(requestBody);
    }

    public Optional<String> getContentType() {

        return requestHeaders.stream()
                .filter((p) -> p.first.equalsIgnoreCase("content-type"))
                .findAny()
                .map(p -> p.second);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public ImmutableSet<Pair<String, String>> getRequestHeaders() {
        return requestHeaders;
    }
}
