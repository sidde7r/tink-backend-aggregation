package se.tink.backend.aggregation.nxgen.http.exceptions;

import se.tink.backend.aggregation.nxgen.http.HttpRequest;

/**
 * A runtime exception thrown by a client handler that signals a failure to process the HTTP request
 * or HTTP response.
 *
 * <p>(Internally wraps Jersey's `ClientHandlerException`)
 */
public class HttpClientException extends RuntimeException {
    private final transient HttpRequest request;

    public HttpClientException(HttpRequest request) {
        this.request = request;
    }

    public HttpClientException(String message, HttpRequest request) {
        super(message);
        this.request = request;
    }

    public HttpClientException(String message, Throwable cause, HttpRequest request) {
        super(message, cause);
        this.request = request;
    }

    public HttpClientException(Throwable cause, HttpRequest request) {
        super(cause.getMessage(), cause);
        this.request = request;
    }

    public HttpClientException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace,
            HttpRequest request) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
