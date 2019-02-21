package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.core.header.OutBoundHeaders;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.Filterable;

public class RequestBuilder extends Filterable<RequestBuilder> {
    private final Filter finalFilter;
    private final String headerAggregatorIdentifier;
    private URL url;
    private Object body;
    private MultivaluedMap<String, Object> headers;
    private List<String> cookies = new ArrayList<>();

    // TODO: REMOVE THIS ONCE AGGREGATOR IDENTIFIER IS VERIFIED
    public static Logger logger = LoggerFactory.getLogger(RequestBuilder.class);

    public RequestBuilder(
            Filterable filterChain,
            Filter finalFilter,
            URL url,
            String headerAggregatorIdentifier) {
        this(filterChain, finalFilter, headerAggregatorIdentifier);
        this.url = url;
    }

    public RequestBuilder(
            Filterable filterChain, Filter finalFilter, String headerAggregatorIdentifier) {
        super(filterChain);
        this.finalFilter = finalFilter;

        // OutBoundHeaders is a case-insensitive MultivaluedMap
        headers = new OutBoundHeaders();
        this.headerAggregatorIdentifier = headerAggregatorIdentifier;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return url.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RequestBuilder) {
            final RequestBuilder that = (RequestBuilder) obj;
            return that.url.equals(this.url);
        }
        return false;
    }

    // UniformInterface
    private HttpRequest build(HttpMethod method) {
        return new HttpRequestImpl(method, url, headers, body);
    }

    /* package */ <T> T raw(Class<T> c, HttpRequest request)
            throws HttpClientException, HttpResponseException {
        return handle(c, request);
    }

    /* package */ void raw(HttpRequest request) throws HttpClientException, HttpResponseException {
        handle(HttpResponse.class, request);
    }

    /**
     * Invoke the HEAD method.
     *
     * @return the HTTP response.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public HttpResponse head() throws HttpClientException {
        return handle(HttpResponse.class, build(HttpMethod.HEAD));
    }

    /**
     * Invoke the OPTIONS method.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T options(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.OPTIONS));
    }

    /**
     * Invoke the GET method.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T get(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.GET));
    }

    /**
     * Invoke the PUT method with no request body or response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void put() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.PUT));
    }

    /**
     * Invoke the PUT method with a request body but no response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @param body the request body.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void put(Object body) throws HttpResponseException, HttpClientException {
        body(body).put();
    }

    /**
     * Invoke the PUT method with no request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T put(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.PUT));
    }

    /**
     * Invoke the PUT method with a request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param body the request body.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T put(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).put(c);
    }

    /**
     * Invoke the POST method with no request body or response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void post() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.POST));
    }

    /**
     * Invoke the POST method with a request body but no response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @param body the request body.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void post(Object body) throws HttpResponseException, HttpClientException {
        body(body).post();
    }

    /**
     * Invoke the POST method with no request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T post(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.POST));
    }

    /**
     * Invoke the POST method with a request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param body the request body.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T post(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).post(c);
    }

    /**
     * Invoke the DELETE method with no request body or response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void delete() throws HttpResponseException, HttpClientException {
        voidHandle(build(HttpMethod.DELETE));
    }

    /**
     * Invoke the DELETE method with a request body but no response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @param body the request body.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void delete(Object body) throws HttpResponseException, HttpClientException {
        body(body).delete();
    }

    /**
     * Invoke the DELETE method with no request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T delete(Class<T> c) throws HttpResponseException, HttpClientException {
        return handle(c, build(HttpMethod.DELETE));
    }

    /**
     * Invoke the DELETE method with a request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param body the request body.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T delete(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return body(body).delete(c);
    }

    /**
     * Invoke a HTTP method with no request body or response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @param method the HTTP method.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void method(HttpMethod method) throws HttpResponseException, HttpClientException {
        voidHandle(build(method));
    }

    /**
     * Invoke a HTTP method with a request body but no response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @param method the HTTP method.
     * @param body the request body.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public void method(HttpMethod method, Object body)
            throws HttpResponseException, HttpClientException {
        body(body).method(method);
    }

    /**
     * Invoke a HTTP method with no request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T method(HttpMethod method, Class<T> c)
            throws HttpResponseException, HttpClientException {
        return handle(c, build(method));
    }

    /**
     * Invoke a HTTP method with a request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @param body the request body.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    public <T> T method(HttpMethod method, Class<T> c, Object body)
            throws HttpResponseException, HttpClientException {
        return body(body).method(method, c);
    }

    /** AbstractForm and its subclasses handled specifivcally */
    public RequestBuilder body(Object body) {
        if (body instanceof AbstractForm) {
            this.body = ((AbstractForm) body).getBodyValue();
        } else {
            this.body = body;
        }
        return this;
    }

    public RequestBuilder body(Object body, MediaType type) {
        body(body);
        type(type);
        return this;
    }

    public RequestBuilder body(Object body, String type) {
        body(body);
        type(type);
        return this;
    }

    public RequestBuilder type(MediaType type) {
        headers.putSingle("Content-Type", type);
        return this;
    }

    public RequestBuilder type(String type) {
        return type(MediaType.valueOf(type));
    }

    public RequestBuilder accept(MediaType... types) {
        for (MediaType type : types) {
            headers.add("Accept", type);
        }
        return this;
    }

    public RequestBuilder accept(String... types) {
        for (String type : types) {
            headers.add("Accept", type);
        }
        return this;
    }

    public RequestBuilder acceptLanguage(Locale... locales) {
        for (Locale locale : locales) {
            headers.add("Accept-Language", locale);
        }
        return this;
    }

    public RequestBuilder acceptLanguage(String... locales) {
        for (String locale : locales) {
            headers.add("Accept-Language", locale);
        }
        return this;
    }

    public RequestBuilder cookie(String name, String value) {
        cookies.add(String.format("%s=%s", name, value));
        return this;
    }

    public RequestBuilder cookie(Cookie cookie) {
        cookies.add(cookie.toString());
        return this;
    }

    public RequestBuilder header(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    public RequestBuilder header(HeaderEnum header) {
        headers.add(header.getKey(), header.getValue());
        return this;
    }

    public RequestBuilder headers(Map<String, Object> map) {
        map.forEach(this::header);
        return this;
    }

    public RequestBuilder headers(MultivaluedMap<String, Object> map) {
        map.forEach((k, l) -> l.forEach(v -> header(k, v)));
        return this;
    }

    public RequestBuilder queryParam(String key, String value) {
        url = url.queryParam(key, value);
        return this;
    }

    public RequestBuilder queryParams(Map<String, String> queryParams) {
        url = url.queryParams(queryParams);
        return this;
    }

    public RequestBuilder queryParams(MultivaluedMap<String, String> queryParams) {
        url = url.queryParams(queryParams);
        return this;
    }

    public RequestBuilder overrideHeader(String name, Object value) {
        headers.remove(name);
        headers.add(name, value);
        return this;
    }

    public RequestBuilder addBasicAuth(String username, String password) {
        String value =
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(
                                        String.format("%s:%s", username, password).getBytes()));
        return header(HttpHeaders.AUTHORIZATION, value);
    }

    public RequestBuilder addBasicAuth(String username) {
        String value =
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(String.format("%s", username).getBytes()));
        return header(HttpHeaders.AUTHORIZATION, value);
    }

    public RequestBuilder addBearerToken(OAuth2Token token) {
        return header(HttpHeaders.AUTHORIZATION, token.toAuthorizeHeader());
    }

    private void addCookiesToHeader() {
        if (!cookies.isEmpty()) {
            headers.add("Cookie", cookies.stream().collect(Collectors.joining("; ")));
        }
    }

    private void addAggregatorToHeader() {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(headerAggregatorIdentifier),
                "Aggregator header identifier is null. The header should not be null");

        if (!headers.containsKey("X-Aggregator")) {
            headers.add("X-Aggregator", headerAggregatorIdentifier);
        }
    }

    private <T> T handle(Class<T> c, HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        // Add the final filter so that we actually send the request
        addFilter(finalFilter);

        addCookiesToHeader();
        addAggregatorToHeader();
        HttpResponse httpResponse = getFilterHead().handle(httpRequest);

        // Throw an exception for all statuses >= 400, i.e. the request was not accepted. This is to
        // force us
        // to handle invalid responses in a unified way (try/catch).
        if (httpResponse.getStatus() >= 400) {
            throw new HttpResponseException(
                    detailedExceptionMessage(httpResponse), httpRequest, httpResponse);
        }

        if (c == HttpResponse.class) {
            return c.cast(httpResponse);
        } else {
            return httpResponse.getBody(c);
        }
    }

    private String detailedExceptionMessage(HttpResponse httpResponse) {
        String message = "Response statusCode: " + httpResponse.getStatus();
        try {
            return message + " with body: " + httpResponse.getBody(String.class);
        } catch (Exception e) {
            // just in case, but should never be reached.
            return message;
        }
    }

    // This is what Jersey does. Since we are not interested in the response data we immediately
    // close the connection.
    private void voidHandle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        addCookiesToHeader();
        addAggregatorToHeader();
        HttpResponse httpResponse = handle(HttpResponse.class, httpRequest);
        if (httpResponse.getStatus() >= 300) {
            // Since we internally request the response type `ClientResponse` (jersey type) we must
            // do this check
            // here (Jersey does it internally if the response type is != `ClientResponse`).
            // Jersey has this exact same check in their `voidHandle` (which we bypass, but want to
            // mimic)
            throw new HttpResponseException(httpRequest, httpResponse);
        }
    }
}
