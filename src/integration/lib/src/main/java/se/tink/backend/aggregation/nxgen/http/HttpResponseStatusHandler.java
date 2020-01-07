package se.tink.backend.aggregation.nxgen.http;

import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public interface HttpResponseStatusHandler {

    void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse);

    void handleResponseWithoutExpectedReturnBody(
            HttpRequest httpRequest, HttpResponse httpResponse);
}
