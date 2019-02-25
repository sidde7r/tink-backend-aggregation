package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class URL {
    public static final Pattern URL_PARAMETER_PATTERN = Pattern.compile("\\{[^{}]{2,}}");
    private static final String URL_SEPARATOR = "/";

    private final String url;
    private final String query;

    public URL(String url) {
        String[] parts = url.split("\\?");
        this.url = parts[0];
        this.query = parts.length > 1 ? parts[1] : null;
    }

    private URL(String url, String query) {
        this.url = url;
        this.query = query;
    }

    private String prependQueryIfPresent(String queryParam) {
        return Strings.isNullOrEmpty(query) ? queryParam : query + "&" + queryParam;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Optional<String> toQueryString(String key, String value) {
        if (Strings.isNullOrEmpty(key) || value == null) {
            return Optional.empty();
        }

        return Optional.of(urlEncode(key) + "=" + urlEncode(value));
    }

    public static String urlDecode(String encodedValue) {
        try {
            return URLDecoder.decode(encodedValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public URL parameter(String key, String value) {
        Preconditions.checkState(!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(value));

        String escapedParameterVariable = Pattern.quote("{" + key + "}");
        Matcher matcher = Pattern.compile(escapedParameterVariable).matcher(url);
        Preconditions.checkState(matcher.find());

        return new URL(matcher.replaceAll(urlEncode(value)), query);
    }

    public URL queryParam(String key, String value) {
        return Optional.of(toQueryString(key, value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::prependQueryIfPresent)
                .map(s -> new URL(url, s))
                .orElse(this);
    }

    public URL queryParams(Map<String, String> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return this;
        }

        return map.entrySet()
                .stream()
                        .map(p -> toQueryString(p.getKey(), p.getValue()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce((p1, p2) -> p1 + "&" + p2)
                        .map(this::prependQueryIfPresent)
                        .map(s -> new URL(url, s))
                        .orElse(this);
    }

    public URL queryParams(MultivaluedMap<String, String> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return this;
        }

        return map.entrySet()
                        .stream()
                        .flatMap(this::multiEntryToQueryString)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce((p1, p2) -> p1 + "&" + p2)
                        .map(this::prependQueryIfPresent)
                        .map(s -> new URL(url, s))
                        .orElse(this);
    }

    private Stream<Optional<String>> multiEntryToQueryString(Map.Entry<String, List<String>> m) {
        return m.getValue().stream().map(p -> toQueryString(m.getKey(), p));
    }

    public URL concat(final String s) {
        return new URL(url.concat(s), query);
    }

    public URL concatWithSeparator(final String s) {
        return new URL(url.concat(URL_SEPARATOR).concat(s), query);
    }

    public String get() {
        String url = this.url;

        if (!Strings.isNullOrEmpty(query)) {
            url += "?" + query;
        }

        return url;
    }

    public String getScheme() {
        return toUri().getScheme();
    }

    public URL getUrl() {
        return new URL(url);
    }

    public URI toUri() {
        // Ensure there are no unpopulated parameters before converting to URI
        Preconditions.checkState(!URL_PARAMETER_PATTERN.matcher(url).find());
        return URI.create(get());
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        URL url1 = (URL) o;

        return new EqualsBuilder().append(url, url1.url).append(query, url1.query).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(url).append(query).toHashCode();
    }
}
