package se.tink.backend.aggregation.nxgen.http;

import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.rate_limit_service.RateLimitService;

public class DefaultResponseStatusHandler implements HttpResponseStatusHandler {

    private final String providerName;

    public DefaultResponseStatusHandler(String providerName) {
        this.providerName = providerName;
    }

    public DefaultResponseStatusHandler() {
        this.providerName = null;
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        // Throw an exception for all statuses >= 400, i.e. the request was not accepted. This is to
        // force us
        // to handle invalid responses in a unified way (try/catch).
        if (httpResponse.getStatus() >= 400) {
            HttpResponseException ex =
                    new HttpResponseException(
                            detailedExceptionMessage(httpResponse), httpRequest, httpResponse);
            if (httpResponse.getStatus() == 429) {
                RateLimitService.INSTANCE.notifyRateLimitExceeded(providerName, ex);
            }
            throw ex;
        }
    }

    @Override
    public void handleResponseWithoutExpectedReturnBody(
            HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 300) {
            // Since we internally request the response type `ClientResponse` (jersey type) we must
            // do this check
            // here (Jersey does it internally if the response type is != `ClientResponse`).
            // Jersey has this exact same check in their `voidHandle` (which we bypass, but want to
            // mimic)
            throw new HttpResponseException(httpRequest, httpResponse);
        }
    }

    protected String detailedExceptionMessage(HttpResponse httpResponse) {
        String message = "Response statusCode: " + httpResponse.getStatus();
        try {
            return message + " with body: " + httpResponse.getBody(String.class);
        } catch (Exception e) {
            // just in case, but should never be reached.
            return message;
        }
    }
}
