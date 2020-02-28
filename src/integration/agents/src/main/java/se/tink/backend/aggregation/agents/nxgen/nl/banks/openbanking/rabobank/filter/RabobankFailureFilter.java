package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RabobankFailureFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        final int status = response.getStatus();
        if (ErrorMessages.ERROR_RESPONSES.equals(status)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Code status : " + status + "Error body : " + response.getBody(String.class));
        }
        return response;
    }
}
