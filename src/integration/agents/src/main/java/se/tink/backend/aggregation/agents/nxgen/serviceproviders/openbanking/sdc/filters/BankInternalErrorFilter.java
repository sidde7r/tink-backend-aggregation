package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.filters;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BankInternalErrorFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isBankSideFailure(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return response;
    }

    public static boolean isBankSideFailure(HttpResponse response) {
        if (response.getStatus() == 400 && response.hasBody()) {
            ErrorResponse errorResponse = getBodyAsExpectedType(response);
            return errorResponse != null && errorResponse.isInternalError();
        }
        return false;
    }

    private static ErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
