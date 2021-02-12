package se.tink.backend.aggregation.nxgen.http.url;

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

/**
 * URL class represents a Uniform Resource Locator.
 *
 * <p>URL has two {@link String} field for the url and a field for the query. When converting the
 * URL object to {@link String} the query (assuming it exists) will start with '?' and query
 * parameters are separated with '&'.
 */
public final class URL {
    private static final Pattern URL_PARAMETER_PATTERN = Pattern.compile("\\{[^{}]{2,}}");
    public static final String URL_SEPARATOR = "/";

    private final String url;
    private final String query;

    /**
     * Constructor that sets the URL without the query parameters to the url field. And sets the
     * query parameters without the URL to the query field.
     *
     * @param url {@link String} containing a URL
     */
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

    public static URL of(String url) {
        return new URL(url);
    }

    public static String urlEncode(String value) {
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

    private Optional<String> toQueryStringRaw(String key, String value) {
        if (Strings.isNullOrEmpty(key) || value == null) {
            return Optional.empty();
        }

        return Optional.of(key + "=" + value);
    }

    /**
     * Decodes a URL that is UTF-8 encoded.
     *
     * @param encodedValue {@link String} containing the UTF-8 encoded URL
     * @return the decoded URL
     */
    public static String urlDecode(String encodedValue) {
        try {
            return URLDecoder.decode(encodedValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Replaces a parameter in a URL with a value.
     *
     * <p>E.g. "http://www.bank.com/{accountId}", accountId is the key that will be replaced with a
     * value.
     *
     * @param key a key in a URL surrounded around curly brackets
     * @param value the value which will be URL-encoded and is to replace the key
     * @return a URL object where the key has replaced the value (curly brackets will also be
     *     removed)
     */
    public URL parameter(String key, String value) {
        return parameter(key, value, true);
    }

    /**
     * Replaces a parameter in a URL without encoding
     *
     * <p>E.g. "http://www.bank.com/{accountId}", accountId is the key that will be replaced with a
     * value.
     *
     * @param key a key in a URL surrounded around curly brackets
     * @param value the value which will replace key.
     * @return a URL object where the key has replaced the value (curly brackets will also be
     *     removed)
     */
    public URL parameterNoEncoding(String key, String value) {
        return parameter(key, value, false);
    }

    private URL parameter(String key, String value, boolean shouldEncodeParameter) {
        Preconditions.checkState(!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(value));

        String escapedParameterVariable = Pattern.quote("{" + key + "}");
        Matcher matcher = Pattern.compile(escapedParameterVariable).matcher(url);
        Preconditions.checkState(matcher.find());

        String parameterValue = shouldEncodeParameter ? urlEncode(value) : value;
        return new URL(matcher.replaceAll(parameterValue), query);
    }

    /**
     * Sets a query parameter to the URL.
     *
     * <p>Both the query key and query value will be URL encoded. If a query parameter already exist
     * then '&' will be prepended to the next query.
     *
     * @param key query name
     * @param value string value for the query value
     * @return a URL object with the query parameter
     */
    public URL queryParam(String key, String value) {
        return toQueryString(key, value)
                .map(this::prependQueryIfPresent)
                .map(s -> new URL(url, s))
                .orElse(this);
    }

    public URL queryParamRaw(String key, String value) {
        return toQueryStringRaw(key, value)
                .map(this::prependQueryIfPresent)
                .map(s -> new URL(url, s))
                .orElse(this);
    }

    /**
     * Sets multiple query parameters at once using {@link Map}
     *
     * @param map key-value map, key is the query names, value is the corresponding value for the
     *     key
     * @return query {@link String} in the form "?{key1}={value1}&{key2}={value2}"
     */
    public URL queryParams(Map<String, String> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return this;
        }

        return map.entrySet().stream()
                .map(p -> toQueryString(p.getKey(), p.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce((p1, p2) -> p1 + "&" + p2)
                .map(this::prependQueryIfPresent)
                .map(s -> new URL(url, s))
                .orElse(this);
    }

    /**
     * Sets multiple query parameters at once using {@link MultivaluedMap}. Applicable when a query
     * key appears repeatedly. E.g. "?id=a&id=b"
     *
     * @param map {@link MultivaluedMap} where a key can map to multiple values
     * @return query {@link String} with query key mapped to multiple values
     */
    public URL queryParams(MultivaluedMap<String, String> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return this;
        }

        return map.entrySet().stream()
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

    /**
     * Appends an endpoint to the URL while preserving any present query
     *
     * @param s Endpoint that is to be concatenated with the URL
     * @return URL with the appended endpoint
     */
    public URL concat(final String s) {
        return new URL(url.concat(s), query);
    }

    /**
     * Appends an endpoint to URL and concatenates '/' in front of the endpoint while preserving any
     * present query.
     *
     * @param s {@link String} that is to be concatenated to the URL
     * @return URL with a '/' before the appended endpoint
     */
    public URL concatWithSeparator(final String s) {
        return new URL(url.concat(URL_SEPARATOR).concat(s), query);
    }

    /**
     * Returns the content of this URL as a {@link String}.
     *
     * <p>If query is also available then it will add the query to the URL with query character '?'
     * in front of the query.
     *
     * @return the {@link String} form of the URL
     */
    public String get() {
        String url = this.url;

        if (!Strings.isNullOrEmpty(query)) {
            url += "?" + query;
        }

        return url;
    }

    /**
     * Converts URL to URI and returns the scheme component of it.
     *
     * @return the scheme component of the URI
     */
    public String getScheme() {
        return toUri().getScheme();
    }

    /**
     * Returns a URL excluding the query.
     *
     * @return URL object of the {@link String} url. Query excluded.
     */
    public URL getUrl() {
        return new URL(url);
    }

    /**
     * Converts the URL {@link String} to a URI object
     *
     * @return the converted URI object of the URL
     */
    public URI toUri() {
        // Ensure there are no unpopulated parameters before converting to URI
        Preconditions.checkState(!URL_PARAMETER_PATTERN.matcher(url).find());
        return URI.create(get());
    }

    /**
     * Returns the content of this URL as a {@link String} .
     *
     * <p>if query exist the '?' will be prepended to the query.
     *
     * @return the {@link String} form of the URL
     */
    @Override
    public String toString() {
        return get();
    }

    /**
     * Test if two URL objects are equal.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        URL url1 = (URL) o;

        return new EqualsBuilder().append(url, url1.url).append(query, url1.query).isEquals();
    }

    /**
     * Returns a hash code value for the object.
     *
     * <p>Two randomly chosen, prime numbers are be passed to the HashCodeBuilder and appended with
     * the url and query
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(url).append(query).toHashCode();
    }
}
