package se.tink.backend.aggregation.nxgen.http.request;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.http.header.AuthorizationHeader;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;

public interface HttpRequestBuilder {

    /** AbstractForm and its subclasses handled specifically */
    HttpRequestBuilder body(Object body);

    /**
     * Method that sets Content-Type header and payload of the request.
     *
     * @param body Payload of the request.
     * @param type Predefined type defined in {@link MediaType}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder body(Object body, MediaType type);

    /**
     * Method that sets Content-Type header and payload of the request.
     *
     * @param body Payload of the request.
     * @param type Content type that will be translated to {@link MediaType}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder body(Object body, String type);

    /**
     * Method that sets Content-Type header of the request.
     *
     * @param type Predefined type defined in {@link MediaType}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder type(MediaType type);

    /**
     * Method that sets Content-Type header of the request.
     *
     * @param type {@link String} that will be translated to {@link MediaType}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder type(String type);

    /**
     * Method that sets what types from {@link MediaType} are accepted by the server in the
     * response. One header per {@link MediaType} will be used.
     *
     * @param types Comma separated values of type {@link MediaType}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder accept(MediaType... types);

    /**
     * Method that sets types accepted by the server. The types does not need to be defined in
     * {@link MediaType}.
     *
     * @param types Comma separated values of type {@link String}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder accept(String... types);

    /**
     * Method that sets Accept-Language headers of the request. One header per {@link Locale} will
     * be used.
     *
     * @param locales Comma separated values of type {@link Locale}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder acceptLanguage(Locale... locales);
    /**
     * Method that sets Accept-Language header of the request. One header per {@link String} will be
     * used.
     *
     * @param locales Comma separated values of type {@link Locale}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder acceptLanguage(String... locales);

    /**
     * Method setting Cookie header of the request. Format of the cookie is `{@param name}={@param
     * value}`.
     *
     * @param name Name of the cookie.
     * @param value Value of the cookie.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder cookie(String name, String value);

    /**
     * Method setting Cookie header of the request.
     *
     * @param cookie Properly prepared object of type {@link Cookie}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder cookie(Cookie cookie);
    /**
     * Method to adding a customized header of the request.
     *
     * @param name Name of the header.
     * @param value Value of the header.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder header(String name, Object value);
    /**
     * Method to adding a customized header of the request using predefined {@link HeaderEnum}.
     *
     * @param header Value of the header.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder header(HeaderEnum header);

    /**
     * Method to add customized headers to request. Each entry in map will be added as separate
     * header, with {@link String} as key which will be used as name and {@link Object} as value.
     *
     * @param map {@link Map} with headers to be set on the request.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder headers(Map<String, Object> map);

    /**
     * Method setting customized headers on the request, in case when some headers can occur
     * multiple times. Each entry in map will be added as separate header, with {@link String} as
     * key which will be used as name and {@link Object} as value.
     *
     * @param map {@link MultivaluedMap} with headers to be set on the request.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder headers(MultivaluedMap<String, Object> map);

    /**
     * Method adding query parameter to the request.
     *
     * @param key Name of the parameter to be set on request.
     * @param value Value of the parameter to be set on request.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder queryParam(String key, String value);

    HttpRequestBuilder queryParamRaw(String key, String value);

    /**
     * Method adding query parameters to the request. Each entry in map will be added as separate
     * query parameter, with {@link String} as key which will be used as name and {@link String} as
     * value.
     *
     * @param queryParams {@link Map} containing query params to be added to the request.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder queryParams(Map<String, String> queryParams);

    /**
     * Method setting customized headers on the request, in case when some of the quary parameters
     * can occur multiple times. Each entry in map will be added as separate header, with {@link
     * String} as key which will be used as name and {@link String} as value.
     *
     * @param queryParams {@link MultivaluedMap} with query params to be set on the request.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder queryParams(MultivaluedMap<String, String> queryParams);

    /**
     * Method for overriding header value. Note that if there are multiple values associated with
     * this {@param name} all of them will be removed and replaced with only one provider in this
     * method.
     *
     * @param name Name of the header which value(s) will be changed.
     * @param value New value for the header specified with {@param name}.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder overrideHeader(String name, Object value);

    /**
     * Method setting value of `Authorization` header on the request. Format of the header value
     * will be `Basic {@param username}:{@param password}`. Where `Basic` is a plain text while
     * {@param username} and {@param password} are {@link Base64} encoded.
     *
     * @param username Username needed for authentication.
     * @param password Password needed for authorization.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder addBasicAuth(String username, String password);

    /**
     * Method setting value of `Authorization` header on the request. Format of the header value
     * will be `Basic {@param username}`. Where `Basic` is a plain text while {@param username} is
     * {@link Base64} encoded.
     *
     * @param username Username needed for authentication.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder addBasicAuth(String username);

    /**
     * Method setting value of `Authorization` header on the request.
     *
     * @param token Ton of type {@link AuthorizationHeader} to be used for authorization of request.
     * @return {@link HttpRequestBuilder} for further use with fluent interface.
     */
    HttpRequestBuilder addBearerToken(AuthorizationHeader token);

    /**
     * Remove the X-Aggregator header from the request. There exist scenarios where it is necessary
     * to conceal the aggregator's identity.
     */
    HttpRequestBuilder removeAggregatorHeader();

    /**
     * Builds Http request with given http methods
     *
     * @param method http method
     * @return {@link HttpRequest} for further use with http client
     */
    HttpRequest build(HttpMethod method);
}
