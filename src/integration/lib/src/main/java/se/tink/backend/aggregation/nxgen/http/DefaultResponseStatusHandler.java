package se.tink.backend.aggregation.nxgen.http;

import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class DefaultResponseStatusHandler implements HttpResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        // Throw an exception for all statuses >= 400, i.e. the request was not accepted. This is to
        // force us
        // to handle invalid responses in a unified way (try/catch).
        if (httpResponse.getStatus() >= 400) {
            throw new HttpResponseException(
                    detailedExceptionMessage(httpResponse), httpRequest, httpResponse);
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
