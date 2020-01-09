package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter;

import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * This filter replicates the transformation from bad responses into a HttpResponseException as
 * performed by the {@link RequestBuilder}, but with a more detailed message for SDC agents, which
 * return the error message in a special header instead of the message body.
 */
public class SdcExceptionFilter extends Filter {

    private static final String ERROR_MESSAGE_HEADER = "X-SDC-ERROR-MESSAGE";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() >= 400) {
            throw new HttpResponseException(
                    detailedExceptionMessage(httpResponse), httpRequest, httpResponse);
        }
        return httpResponse;
    }

    private String detailedExceptionMessage(HttpResponse httpResponse) {
        String message =
                "Response statusCode: "
                        + httpResponse.getStatus()
                        + "\n    with "
                        + ERROR_MESSAGE_HEADER
                        + " header: "
                        + httpResponse.getHeaders().getFirst(ERROR_MESSAGE_HEADER);
        try {
            return message + "\n    with body: " + httpResponse.getBody(String.class);
        } catch (Exception e) {
            // just in case, but should never be reached.
            return message;
        }
    }
}
