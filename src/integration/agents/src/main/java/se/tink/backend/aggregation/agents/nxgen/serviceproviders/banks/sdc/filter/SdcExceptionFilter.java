package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
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

    private static final Map<String, Supplier<? extends RuntimeException>>
            knownErrorMessagesMapping = new HashMap<>();

    static {
        knownErrorMessagesMapping.put(
                "Your PIN code is blocked. You can create a new PIN in the netbank or contact your bank.",
                () ->
                        new LoginException(
                                LoginError.INCORRECT_CREDENTIALS, "Your PIN code is blocked."));
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() >= 400) {
            handleKnownErrors(httpResponse);
            throw new HttpResponseException(
                    detailedExceptionMessage(httpResponse), httpRequest, httpResponse);
        }
        return httpResponse;
    }

    private void handleKnownErrors(HttpResponse httpResponse) throws AuthenticationException {
        String errorMessage = httpResponse.getHeaders().getFirst(ERROR_MESSAGE_HEADER).trim();
        Supplier<? extends RuntimeException> exceptionSupplier =
                knownErrorMessagesMapping.get(errorMessage);
        if (exceptionSupplier != null) {
            throw exceptionSupplier.get();
        }
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
