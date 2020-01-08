package se.tink.backend.aggregation.nxgen.http.response;

import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

/**
 * A runtime exception thrown by a http method or HttpResponse when the status code of the HTTP
 * response indicates a response that is not expected.
 *
 * <p>(Internally wraps Jersey's `UniformInterfaceException`)
 */
public class HttpResponseException extends RuntimeException {
    private final transient HttpRequest request;
    private final transient HttpResponse response;

    public HttpResponseException(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpResponseException(String message, HttpRequest request, HttpResponse response) {
        super(message);
        this.request = request;
        this.response = response;
    }

    public HttpResponseException(
            String message, Throwable cause, HttpRequest request, HttpResponse response) {
        super(message, cause);
        this.request = request;
        this.response = response;
    }

    public HttpResponseException(Throwable cause, HttpRequest request, HttpResponse response) {
        super(cause.getMessage(), cause);
        this.request = request;
        this.response = response;
    }

    public HttpResponseException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace,
            HttpRequest request,
            HttpResponse response) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.request = request;
        this.response = response;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
