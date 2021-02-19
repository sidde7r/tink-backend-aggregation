package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.filters;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants.Errors.SYSTEM_CLOSED;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SparebankenVestKnownErrorsFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isBankSideFailure(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        return response;
    }

    private boolean isBankSideFailure(HttpResponse response) {
        return response.getStatus() == 403
                && response.getBody(String.class).contains(SYSTEM_CLOSED);
    }
}
