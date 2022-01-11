package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class OpBankServiceUnavailableFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        handleResponse(response);
        return response;
    }

    private void handleResponse(HttpResponse response) {
        String body = response.getBody(String.class).toLowerCase();
        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE
                || body.contains(ErrorMessages.SERVICE_UNAVAILABLE.toLowerCase())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
