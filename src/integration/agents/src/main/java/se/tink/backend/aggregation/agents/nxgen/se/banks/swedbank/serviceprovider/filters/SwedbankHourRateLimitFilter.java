package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TppErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankHourRateLimitFilter extends Filter {
    private static final int ACCESS_EXCEEDED_ERROR_CODE = 429;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        TppErrorResponse errorResponse = response.getBody(TppErrorResponse.class);
        if (response.getStatus() == ACCESS_EXCEEDED_ERROR_CODE
                && errorResponse != null
                && errorResponse
                        .getErrorMessage()
                        .contains(ErrorMessage.REACHED_HOUR_REQUESTS_LIMIT)) {
            throw BankServiceError.ACCESS_EXCEEDED.exception();
        }

        return response;
    }
}
