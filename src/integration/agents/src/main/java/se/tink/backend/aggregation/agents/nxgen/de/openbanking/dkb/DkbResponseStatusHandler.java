package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class DkbResponseStatusHandler implements HttpResponseStatusHandler {

    private final DefaultResponseStatusHandler defaultResponseStatusHandler;

    DkbResponseStatusHandler() {
        this(new DefaultResponseStatusHandler());
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 400) {
            defaultResponseStatusHandler.handleResponse(httpRequest, httpResponse);
        }
    }

    @Override
    public void handleResponseWithoutExpectedReturnBody(
            HttpRequest httpRequest, HttpResponse httpResponse) {
        defaultResponseStatusHandler.handleResponseWithoutExpectedReturnBody(
                httpRequest, httpResponse);
    }
}
