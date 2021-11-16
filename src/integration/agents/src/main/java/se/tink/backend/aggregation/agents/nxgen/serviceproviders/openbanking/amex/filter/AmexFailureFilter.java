package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class AmexFailureFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        try {
            return nextFilter(httpRequest);
        } catch (HttpClientException e) {
            if (isBankSideFailure(e)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw e;
        }
    }

    public static boolean isBankSideFailure(HttpClientException e) {
        return e.getMessage()
                .contains(AmericanExpressConstants.ErrorMessages.API_FAILED_TO_RESPOND);
    }
}
