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

    /**
     * @return Hashcode of request {@link URL}
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }

    /**
     * @return Request {@link URL} in {@link String} format.
     */
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

    /** AbstractForm and its subclasses handled specifically */
    public RequestBuilder body(Object body) {
        if (body instanceof AbstractForm) {
            this.body = ((AbstractForm) body).getBodyValue();
        } else {
            this.body = body;
        }
        return this;
    }

    /**
     * Method that sets Content-Type header and payload of the request.
     * @param body Payload of the request.
     * @param type Predefined type defined in {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder body(Object body, MediaType type) {
        body(body);
        type(type);
        return this;
    }

    /**
     * Method that sets Content-Type header and payload of the request.
     * @param body Payload of the request.
     * @param type Content type that will be translated to {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder body(Object body, String type) {
        body(body);
        type(type);
        return this;
    }

    /**
     * Method that sets Content-Type header of the request.
     * @param type Predefined type defined in {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder type(MediaType type) {
        headers.putSingle("Content-Type", type);
        return this;
    }

    /**
     * Method that sets Content-Type header of the request.
     * @param type {@link String} that will be translated to {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder type(String type) {
        return type(MediaType.valueOf(type));
    }

    /**
     * Method that sets what types from {@link MediaType} are accepted by the server in the response. One header per {@link MediaType} will be used.
     * @param types Comma separated values of type {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder accept(MediaType... types) {
        for (MediaType type : types) {
            headers.add("Accept", type);
        }
        return this;
    }

    /**
     * Method that sets types accepted by the server. The types does not need to be defined in {@link MediaType}.
     * @param types Comma separated values of type {@link String}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder accept(String... types) {
        for (String type : types) {
            headers.add("Accept", type);
        }
        return this;
    }

    /**
     * Method that sets Accept-Language headers of the request. One header per {@link Locale} will be used.
     * @param locales Comma separated values of type {@link Locale}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder acceptLanguage(Locale... locales) {
        for (Locale locale : locales) {
            headers.add("Accept-Language", locale);
        }
        return this;
    }
    /**
     * Method that sets Accept-Language header of the request. One header per {@link String} will be used.
     * @param locales Comma separated values of type {@link Locale}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder acceptLanguage(String... locales) {
        for (String locale : locales) {
            headers.add("Accept-Language", locale);
        }
        return this;
    }

    /**
     * Method setting Cookie header of the request. Format of the cookie is `{@param name}={@param value}`.
     * @param name Name of the cookie.
     * @param value Value of the cookie.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder cookie(String name, String value) {
        cookies.add(String.format("%s=%s", name, value));
        return this;
    }

    /**
     * Method setting Cookie header of the request.
     * @param cookie Properly prepared object of type {@link Cookie}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder cookie(Cookie cookie) {
        cookies.add(cookie.toString());
        return this;
    }

    /**
     * Method to adding a customized header of the request.
     * @param name Name of the header.
     * @param value Value of the header.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder header(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    /**
     * Method to adding a customized header of the request using predefined {@link HeaderEnum}.
     * @param header Value of the header.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder header(HeaderEnum header) {
        headers.add(header.getKey(), header.getValue());
        return this;
    }

    /**
     * Method to add customized headers to request. Each entry in map will be added as separate header,
     * with {@link String} as key which will be used as name and {@link Object} as value.
     * @param map {@link Map} with headers to be set on the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder headers(Map<String, Object> map) {
        map.forEach(this::header);
        return this;
    }

    /**
     * Method setting customized headers on the request, in case when some headers can occur multiple times.
     * Each entry in map will be added as separate header,
     * with {@link String} as key which will be used as name and {@link Object} as value.
     * @param map {@link MultivaluedMap} with headers to be set on the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder headers(MultivaluedMap<String, Object> map) {
        map.forEach((k, l) -> l.forEach(v -> header(k, v)));
        return this;
    }

    /**
     * Method adding query parameter to the request.
     * @param key Name of the parameter to be set on request.
     * @param value Value of the parameter to be set on request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder queryParam(String key, String value) {
        url = url.queryParam(key, value);
        return this;
    }

    /**
     * Method adding query parameters to the request. Each entry in map will be added as separate query parameter,
     * with {@link String} as key which will be used as name and {@link String} as value.
     * @param queryParams {@link Map} containing query params to be added to the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder queryParams(Map<String, String> queryParams) {
        url = url.queryParams(queryParams);
        return this;
    }

    /**
     * Method setting customized headers on the request, in case when some of the quary parameters can occur multiple times.
     * Each entry in map will be added as separate header,
     * with {@link String} as key which will be used as name and {@link String} as value.
     * @param queryParams {@link MultivaluedMap} with query params to be set on the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder queryParams(MultivaluedMap<String, String> queryParams) {
        url = url.queryParams(queryParams);
        return this;
    }

    /**
     * Method for overriding header value. Note that if there are multiple values associated with this {@param name}
     * all of them will be removed and replaced with only one provider in this method.
     * @param name Name of the header which value(s) will be changed.
     * @param value New value for the header specified with {@param name}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder overrideHeader(String name, Object value) {
        headers.remove(name);
        headers.add(name, value);
        return this;
    }

    /**
     * Method setting value of `Authorization` header on the request. Format of the header value will be
     * `Basic {@param username}:{@param password}`. Where `Basic` is a plain text while {@param username} and {@param password}
     * are {@link Base64} encoded.
     * @param username Username needed for authentication.
     * @param password Password needed for authorization.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder addBasicAuth(String username, String password) {
        String value =
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(
                                        String.format("%s:%s", username, password).getBytes()));
        return header(HttpHeaders.AUTHORIZATION, value);
    }
    /**
     * Method setting value of `Authorization` header on the request. Format of the header value will be
     * `Basic {@param username}`. Where `Basic` is a plain text while {@param username}
     * is {@link Base64} encoded.
     * @param username Username needed for authentication.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    public RequestBuilder addBasicAuth(String username) {
        String value =
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(String.format("%s", username).getBytes()));
        return header(HttpHeaders.AUTHORIZATION, value);
    }

    /**
     * Method setting value of `Authorization` header on the request.
     * @param token Ton of type {@link OAuth2Token} to be used for authorization of request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
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
