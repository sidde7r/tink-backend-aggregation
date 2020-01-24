package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ICSBankFailureFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getBody(String.class).contains(ErrorMessages.STATUS_CODE_500)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    ErrorMessages.STATUS_CODE_500 + ": Bank services unavailable.");
        }

        return response;
    }
}
