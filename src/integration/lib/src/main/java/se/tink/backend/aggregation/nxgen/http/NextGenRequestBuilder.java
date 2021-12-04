package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.core.header.OutBoundHeaders;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.NextGenFilterable;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.header.AuthorizationHeader;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NextGenRequestBuilder extends NextGenFilterable<RequestBuilder>
        implements RequestBuilder {

    private final String headerAggregatorIdentifier;
    private URL url;
    private Object body;
    private MultivaluedMap<String, Object> headers;
    private List<String> cookies = new ArrayList<>();
    private HttpResponseStatusHandler responseStatusHandler;
    private boolean shouldAddAggregatorHeader = true;

    public NextGenRequestBuilder(
            List<Filter> filters,
            URL url,
            String headerAggregatorIdentifier,
            HttpResponseStatusHandler responseStatusHandler) {
        this(filters, headerAggregatorIdentifier, responseStatusHandler);
        this.url = url;
    }

    public NextGenRequestBuilder(
            List<Filter> filters,
            String headerAggregatorIdentifier,
            HttpResponseStatusHandler responseStatusHandler) {
        super(filters);

        // OutBoundHeaders is a case-insensitive MultivaluedMap
        headers = new OutBoundHeaders();
        this.headerAggregatorIdentifier = headerAggregatorIdentifier;
        this.responseStatusHandler = responseStatusHandler;
    }

    public URL getUrl() {
        return url;
    }

    /** @return Hashcode of request {@link URL} */
    public int hashCode() {
        return url.hashCode();
    }

    /** @return Request {@link URL} in {@link String} format. */
    public String toString() {
        return url.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NextGenRequestBuilder) {
            final NextGenRequestBuilder that = (NextGenRequestBuilder) obj;
            return that.url.equals(this.url);
        }
        return false;
    }

    // UniformInterface
    public HttpRequest build(HttpMethod method) {
        addCookiesToHeader();
        return new HttpRequestImpl(method, url, headers, body);
    }

    /* package */ <T> T raw(Class<T> c, HttpRequest request)
            throws HttpClientException, HttpResponseException {
        return handle(c, request);
    }

    /* package */ void raw(HttpRequest request) throws HttpClientException, HttpResponseException {
        handle(HttpResponse.class, request);
    }

    public HttpResponse head() throws HttpClientException {
        return handle(HttpResponse.class, build(HttpMethod.HEAD));
    }

    public <T> T options(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.OPTIONS));
    }

    public <T> T get(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.GET));
    }

    public void put() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.PUT));
    }

    public void put(Object body) throws HttpResponseException, HttpClientException {
        body(body).put();
    }

    public <T> T put(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.PUT));
    }

    public <T> T put(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).put(c);
    }

    public void patch() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.PATCH));
    }

    public void patch(Object body) throws HttpResponseException, HttpClientException {
        body(body).patch();
    }

    public <T> T patch(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.PATCH));
    }

    public <T> T patch(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).patch(c);
    }

    public void post() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.POST));
    }

    public void post(Object body) throws HttpResponseException, HttpClientException {
        body(body).post();
    }

    public <T> T post(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.POST));
    }

    public <T> T post(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).post(c);
    }

    public void delete() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.DELETE));
    }

    public void delete(Object body) throws HttpResponseException, HttpClientException {
        body(body).delete();
    }

    public <T> T delete(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.DELETE));
    }

    public <T> T delete(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).delete(c);
    }

    public void method(HttpMethod method) throws HttpResponseException, HttpClientException {
        voidHandle(build(method));
    }

    public void method(HttpMethod method, Object body)
            throws HttpResponseException, HttpClientException {
        body(body).method(method);
    }

    public <T> T method(HttpMethod method, Class<T> c)
            throws HttpResponseException, HttpClientException {
        return handle(c, build(method));
    }

    public <T> T method(HttpMethod method, Class<T> c, Object body)
            throws HttpResponseException, HttpClientException {
        return body(body).method(method, c);
    }

    public NextGenRequestBuilder body(Object body) {
        if (body instanceof AbstractForm) {
            this.body = ((AbstractForm) body).getBodyValue();
        } else {
            this.body = body;
        }
        return this;
    }

    public NextGenRequestBuilder body(Object body, MediaType type) {
        body(body);
        type(type);
        return this;
    }

    public NextGenRequestBuilder body(Object body, String type) {
        body(body);
        type(type);
        return this;
    }

    public NextGenRequestBuilder type(MediaType type) {
        headers.putSingle("Content-Type", type);
        return this;
    }

    public NextGenRequestBuilder type(String type) {
        return type(MediaType.valueOf(type));
    }

    public NextGenRequestBuilder accept(MediaType... types) {
        for (MediaType type : types) {
            headers.add("Accept", type);
        }
        return this;
    }

    public NextGenRequestBuilder accept(String... types) {
        for (String type : types) {
            headers.add("Accept", type);
        }
        return this;
    }

    public NextGenRequestBuilder acceptLanguage(Locale... locales) {
        for (Locale locale : locales) {
            headers.add("Accept-Language", locale);
        }
        return this;
    }

    public NextGenRequestBuilder acceptLanguage(String... locales) {
        for (String locale : locales) {
            headers.add("Accept-Language", locale);
        }
        return this;
    }

    public NextGenRequestBuilder cookie(String name, String value) {
        cookies.add(String.format("%s=%s", name, value));
        return this;
    }

    public NextGenRequestBuilder cookie(Cookie cookie) {
        cookies.add(cookie.toString());
        return this;
    }

    public NextGenRequestBuilder header(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    public NextGenRequestBuilder header(HeaderEnum header) {
        headers.add(header.getKey(), header.getValue());
        return this;
    }

    public NextGenRequestBuilder headers(Map<String, Object> map) {
        map.forEach(this::header);
        return this;
    }

    public NextGenRequestBuilder headers(MultivaluedMap<String, Object> map) {
        map.forEach((k, l) -> l.forEach(v -> header(k, v)));
        return this;
    }

    public NextGenRequestBuilder queryParam(String key, String value) {
        url = url.queryParam(key, value);
        return this;
    }

    public NextGenRequestBuilder queryParamRaw(String key, String value) {
        url = url.queryParamRaw(key, value);
        return this;
    }

    public NextGenRequestBuilder queryParams(Map<String, String> queryParams) {
        url = url.queryParams(queryParams);
        return this;
    }

    public NextGenRequestBuilder queryParams(MultivaluedMap<String, String> queryParams) {
        url = url.queryParams(queryParams);
        return this;
    }

    public NextGenRequestBuilder overrideHeader(String name, Object value) {
        headers.remove(name);
        headers.add(name, value);
        return this;
    }

    public NextGenRequestBuilder addBasicAuth(String username, String password) {
        String value =
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(
                                        String.format("%s:%s", username, password).getBytes()));
        return header(HttpHeaders.AUTHORIZATION, value);
    }

    public NextGenRequestBuilder addBasicAuth(String username) {
        String value =
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(String.format("%s", username).getBytes()));
        return header(HttpHeaders.AUTHORIZATION, value);
    }

    public NextGenRequestBuilder addBearerToken(AuthorizationHeader token) {
        return header(HttpHeaders.AUTHORIZATION, token.toAuthorizeHeader());
    }

    private void addCookiesToHeader() {
        if (!cookies.isEmpty()) {
            headers.add("Cookie", cookies.stream().collect(Collectors.joining("; ")));
        }
    }

    private void addAggregatorToHeader() {

        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(headerAggregatorIdentifier),
                "Aggregator header identifier is null. The header should not be null");

        if (!headers.containsKey("X-Aggregator")) {
            headers.add("X-Aggregator", headerAggregatorIdentifier);
        }
    }

    @Override
    public RequestBuilder removeAggregatorHeader() {
        shouldAddAggregatorHeader = false;
        return this;
    }

    private <T> T handle(Class<T> c, HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        if (shouldAddAggregatorHeader) {
            addAggregatorToHeader();
        }

        reorderFilters();
        HttpResponse httpResponse = getFilterHead().handle(httpRequest);

        if (Objects.nonNull(responseStatusHandler)) {
            responseStatusHandler.handleResponse(httpRequest, httpResponse);
        }

        if (c == HttpResponse.class) {
            return c.cast(httpResponse);
        } else {
            return httpResponse.getBody(c);
        }
    }

    // This is what Jersey does. Since we are not interested in the response data we immediately
    // close the connection.
    private void voidHandle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        if (shouldAddAggregatorHeader) {
            addAggregatorToHeader();
        }

        reorderFilters();
        HttpResponse httpResponse = handle(HttpResponse.class, httpRequest);

        if (Objects.nonNull(responseStatusHandler)) {
            responseStatusHandler.handleResponseWithoutExpectedReturnBody(
                    httpRequest, httpResponse);
        }
    }
}
