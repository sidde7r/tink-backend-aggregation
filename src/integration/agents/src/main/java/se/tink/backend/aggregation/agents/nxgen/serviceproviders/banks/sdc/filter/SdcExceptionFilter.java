package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.Headers;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * This filter replicates the transformation from bad responses into a HttpResponseException as
 * performed by the {@link RequestBuilder}, but with a more detailed message for SDC agents, which
 * return the error message in a special header instead of the message body. In some cases Special
 * Header is not returned but json with pin invalid message is returned.
 */
public class SdcExceptionFilter extends Filter {

    private static final Map<String, String> knownErrorMessagesMapping = new HashMap<>();

    static {
        knownErrorMessagesMapping.put(
                ErrorMessage.PIN_4_CHARACTERS.getCriteria(), "Your PIN has illegal characters.");
        knownErrorMessagesMapping.put(
                ErrorMessage.PIN_BLOCKED.getCriteria(), "Your PIN code is blocked.");
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() >= 400) {
            handleIntegrationError(httpRequest, httpResponse);
        }
        return httpResponse;
    }

    private void handleIntegrationError(HttpRequest httpRequest, HttpResponse httpResponse) {
        handleKnownErrors(httpResponse);
        handleOtherErrors(httpRequest, httpResponse);
    }

    private void handleKnownErrors(HttpResponse httpResponse) {
        Optional.ofNullable(knownErrorMessagesMapping.get(lookForErrorMessage(httpResponse)))
                .ifPresent(
                        knownAuthError -> {
                            throw LoginError.INCORRECT_CREDENTIALS.exception(knownAuthError);
                        });
    }

    private void handleOtherErrors(HttpRequest httpRequest, HttpResponse httpResponse) {
        throw new HttpResponseException(
                detailedExceptionMessage(httpResponse), httpRequest, httpResponse);
    }

    private String lookForErrorMessage(HttpResponse httpResponse) {
        String errorMessage = getErrorMessageFromHeader(httpResponse);
        return errorMessage != null ? errorMessage : checkErrorsInResponseBody(httpResponse);
    }

    private String checkErrorsInResponseBody(HttpResponse httpResponse) {
        String responseBody = httpResponse.getBody(String.class);
        if (ErrorMessage.PIN_4_CHARACTERS.getCriteria().equals(responseBody)) {
            return responseBody;
        }
        return null;
    }

    private String detailedExceptionMessage(HttpResponse httpResponse) {
        String errorMessage = getErrorMessageFromHeader(httpResponse);
        String message =
                "Response statusCode: "
                        + httpResponse.getStatus()
                        + "\n    with "
                        + Headers.X_SDC_ERROR_MESSAGE
                        + " header: "
                        + errorMessage;
        try {
            return message + "\n    with body: " + httpResponse.getBody(String.class);
        } catch (Exception e) {
            // just in case, but should never be reached.
            return message;
        }
    }

    private String getErrorMessageFromHeader(HttpResponse httpResponse) {
        MultivaluedMap<String, String> headers = httpResponse.getHeaders();
        if (headers.containsKey(Headers.X_SDC_ERROR_MESSAGE)) {
            return headers.getFirst(Headers.X_SDC_ERROR_MESSAGE);
        }
        return null;
    }
}
