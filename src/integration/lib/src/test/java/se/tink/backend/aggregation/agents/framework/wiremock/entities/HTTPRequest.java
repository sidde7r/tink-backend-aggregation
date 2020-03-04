package se.tink.backend.aggregation.agents.framework.wiremock.entities;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
    private final List<NameValuePair> query;
    private final List<Pair<String, String>> requestHeaders;
    private final String requestBody;

    public HTTPRequest(
            String method,
            String url,
            List<Pair<String, String>> requestHeaders,
            String requestBody) {

        URI uri = URI.create(url);

        this.path = uri.getRawPath();
        this.query = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.name());

        this.method = method;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
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

    public HTTPRequest(String method, String url, List<Pair<String, String>> requestHeaders) {
        this(method, url, requestHeaders, null);
    }

    public List<NameValuePair> getQuery() {
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

    public List<Pair<String, String>> getRequestHeaders() {
        return requestHeaders;
    }
}
