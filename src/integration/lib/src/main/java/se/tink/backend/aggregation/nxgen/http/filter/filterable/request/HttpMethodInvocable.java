package se.tink.backend.aggregation.nxgen.http.filter.filterable.request;

import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public interface HttpMethodInvocable {

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
}
