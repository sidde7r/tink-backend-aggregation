package se.tink.backend.aggregation.nxgen.http.filter.filterable.request;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.Filterable;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface RequestBuilder extends Filterable<RequestBuilder> {

    URL getUrl();

    /** @return Hashcode of request {@link URL} */
    int hashCode();

    /** @return Request {@link URL} in {@link String} format. */
    @Override
    String toString();

    @Override
    boolean equals(Object obj);

    /**
     * Invoke the HEAD method.
     *
     * @return the HTTP response.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    HttpResponse head() throws HttpClientException;

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
    <T> T options(Class<T> c) throws HttpResponseException, HttpClientException;

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
    <T> T get(Class<T> c) throws HttpResponseException, HttpClientException;

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
    void put() throws HttpResponseException, HttpClientException;

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
    void put(Object body) throws HttpResponseException, HttpClientException;
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
    <T> T put(Class<T> c) throws HttpResponseException, HttpClientException;

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
    <T> T put(Class<T> c, Object body) throws HttpResponseException, HttpClientException;

    ///

    /**
     * Invoke the PATCH method with no request body or response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    void patch() throws HttpResponseException, HttpClientException;

    /**
     * Invoke the PATCH method with a request body but no response.
     *
     * <p>If the status code is less than 300 and a representation is present then that
     * representation is ignored.
     *
     * @param body the request body.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    void patch(Object body) throws HttpResponseException, HttpClientException;

    /**
     * Invoke the PATCH method with no request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    <T> T patch(Class<T> c) throws HttpResponseException, HttpClientException;
    /**
     * Invoke the PATCH method with a request body that returns a response.
     *
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param body the request body.
     * @return an instance of type <code>c</code>.
     * @throws HttpResponseException if the status of the HTTP response is greater than or equal to
     *     300 and <code>c</code> is not the type {@link HttpResponse}.
     * @throws HttpClientException if the client handler fails to process the request or response.
     */
    <T> T patch(Class<T> c, Object body) throws HttpResponseException, HttpClientException;
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
    void post() throws HttpResponseException, HttpClientException;

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
    void post(Object body) throws HttpResponseException, HttpClientException;

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
    <T> T post(Class<T> c) throws HttpResponseException, HttpClientException;

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
    <T> T post(Class<T> c, Object body) throws HttpResponseException, HttpClientException;

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
    void delete() throws HttpResponseException, HttpClientException;
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
    void delete(Object body) throws HttpResponseException, HttpClientException;

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
    <T> T delete(Class<T> c) throws HttpResponseException, HttpClientException;

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
    <T> T delete(Class<T> c, Object body) throws HttpResponseException, HttpClientException;

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
    void method(HttpMethod method) throws HttpResponseException, HttpClientException;

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
    void method(HttpMethod method, Object body);

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
    <T> T method(HttpMethod method, Class<T> c);

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
    <T> T method(HttpMethod method, Class<T> c, Object body)
            throws HttpResponseException, HttpClientException;

    /** AbstractForm and its subclasses handled specifically */
    RequestBuilder body(Object body);

    /**
     * Method that sets Content-Type header and payload of the request.
     *
     * @param body Payload of the request.
     * @param type Predefined type defined in {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder body(Object body, MediaType type);

    /**
     * Method that sets Content-Type header and payload of the request.
     *
     * @param body Payload of the request.
     * @param type Content type that will be translated to {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder body(Object body, String type);

    /**
     * Method that sets Content-Type header of the request.
     *
     * @param type Predefined type defined in {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder type(MediaType type);

    /**
     * Method that sets Content-Type header of the request.
     *
     * @param type {@link String} that will be translated to {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder type(String type);

    /**
     * Method that sets what types from {@link MediaType} are accepted by the server in the
     * response. One header per {@link MediaType} will be used.
     *
     * @param types Comma separated values of type {@link MediaType}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder accept(MediaType... types);

    /**
     * Method that sets types accepted by the server. The types does not need to be defined in
     * {@link MediaType}.
     *
     * @param types Comma separated values of type {@link String}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder accept(String... types);

    /**
     * Method that sets Accept-Language headers of the request. One header per {@link Locale} will
     * be used.
     *
     * @param locales Comma separated values of type {@link Locale}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder acceptLanguage(Locale... locales);
    /**
     * Method that sets Accept-Language header of the request. One header per {@link String} will be
     * used.
     *
     * @param locales Comma separated values of type {@link Locale}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder acceptLanguage(String... locales);

    /**
     * Method setting Cookie header of the request. Format of the cookie is `{@param name}={@param
     * value}`.
     *
     * @param name Name of the cookie.
     * @param value Value of the cookie.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder cookie(String name, String value);

    /**
     * Method setting Cookie header of the request.
     *
     * @param cookie Properly prepared object of type {@link Cookie}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder cookie(Cookie cookie);
    /**
     * Method to adding a customized header of the request.
     *
     * @param name Name of the header.
     * @param value Value of the header.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder header(String name, Object value);
    /**
     * Method to adding a customized header of the request using predefined {@link HeaderEnum}.
     *
     * @param header Value of the header.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder header(HeaderEnum header);

    /**
     * Method to add customized headers to request. Each entry in map will be added as separate
     * header, with {@link String} as key which will be used as name and {@link Object} as value.
     *
     * @param map {@link Map} with headers to be set on the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder headers(Map<String, Object> map);

    /**
     * Method setting customized headers on the request, in case when some headers can occur
     * multiple times. Each entry in map will be added as separate header, with {@link String} as
     * key which will be used as name and {@link Object} as value.
     *
     * @param map {@link MultivaluedMap} with headers to be set on the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder headers(MultivaluedMap<String, Object> map);

    /**
     * Method adding query parameter to the request.
     *
     * @param key Name of the parameter to be set on request.
     * @param value Value of the parameter to be set on request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder queryParam(String key, String value);

    RequestBuilder queryParamRaw(String key, String value);

    /**
     * Method adding query parameters to the request. Each entry in map will be added as separate
     * query parameter, with {@link String} as key which will be used as name and {@link String} as
     * value.
     *
     * @param queryParams {@link Map} containing query params to be added to the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder queryParams(Map<String, String> queryParams);

    /**
     * Method setting customized headers on the request, in case when some of the quary parameters
     * can occur multiple times. Each entry in map will be added as separate header, with {@link
     * String} as key which will be used as name and {@link String} as value.
     *
     * @param queryParams {@link MultivaluedMap} with query params to be set on the request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder queryParams(MultivaluedMap<String, String> queryParams);

    /**
     * Method for overriding header value. Note that if there are multiple values associated with
     * this {@param name} all of them will be removed and replaced with only one provider in this
     * method.
     *
     * @param name Name of the header which value(s) will be changed.
     * @param value New value for the header specified with {@param name}.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder overrideHeader(String name, Object value);

    /**
     * Method setting value of `Authorization` header on the request. Format of the header value
     * will be `Basic {@param username}:{@param password}`. Where `Basic` is a plain text while
     * {@param username} and {@param password} are {@link Base64} encoded.
     *
     * @param username Username needed for authentication.
     * @param password Password needed for authorization.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder addBasicAuth(String username, String password);

    /**
     * Method setting value of `Authorization` header on the request. Format of the header value
     * will be `Basic {@param username}`. Where `Basic` is a plain text while {@param username} is
     * {@link Base64} encoded.
     *
     * @param username Username needed for authentication.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder addBasicAuth(String username);

    /**
     * Method setting value of `Authorization` header on the request.
     *
     * @param token Ton of type {@link OAuth2Token} to be used for authorization of request.
     * @return {@link RequestBuilder} for further use with fluent interface.
     */
    RequestBuilder addBearerToken(OAuth2Token token);

    /**
     * Remove the X-Aggregator header from the request. There exist scenarios where it is necessary
     * to conceal the aggregator's identity.
     */
    RequestBuilder removeAggregatorHeader();
}
