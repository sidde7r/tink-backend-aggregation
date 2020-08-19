package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.filter;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentRuntimeError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class KnownErrorsFilter extends Filter {

    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String INT_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        ErrorResponse errorResponse;
        try {
            errorResponse = httpResponse.getBody(ErrorResponse.class);
        } catch (HttpClientException hce) {
            // Could not parse as expected body, meaning this is definitely not known exception
            return httpResponse;
        }

        checkForKnownError(
                httpResponse,
                errorResponse,
                400,
                150,
                BAD_REQUEST,
                BankServiceError.NO_BANK_SERVICE);
        checkForKnownError(
                httpResponse,
                errorResponse,
                500,
                300,
                INT_SERVER_ERROR,
                BankServiceError.BANK_SIDE_FAILURE);
        checkForKnownError(
                httpResponse,
                errorResponse,
                500,
                300,
                BAD_REQUEST,
                BankServiceError.BANK_SIDE_FAILURE);
        return httpResponse;
    }

    private void checkForKnownError(
            HttpResponse httpResponse,
            ErrorResponse errorResponse,
            int httpStatus,
            int errorCode,
            String errorStatus,
            AgentRuntimeError toThrow) {
        if (httpResponse.getStatus() == httpStatus
                && errorResponse != null
                && errorResponse.getErrorCode() == errorCode
                && errorStatus.equalsIgnoreCase(errorResponse.getStatus())) {
            throw toThrow.exception(httpResponse.getBody(String.class));
        }
    }
}
