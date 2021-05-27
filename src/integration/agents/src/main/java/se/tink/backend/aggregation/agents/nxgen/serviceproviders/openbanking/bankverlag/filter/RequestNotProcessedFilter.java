package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.PsuErrorMessages;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RequestNotProcessedFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if ((response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR)
                && response.getBody(String.class)
                        .contains(PsuErrorMessages.REQUEST_PROCESSING_ERROR)) {
            throw BankServiceError.NO_BANK_SERVICE.exception(
                    "Http status: " + HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
