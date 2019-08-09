package se.tink.backend.aggregation.nxgen.http;

public interface HttpResponseStatusHandler {

    void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse);

    void handleResponseWithoutExpectedReturnBody(
            HttpRequest httpRequest, HttpResponse httpResponse);
}
