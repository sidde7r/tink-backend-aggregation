package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class KnabFailureFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse httpResponse = nextFilter(httpRequest);

        final int status = httpResponse.getStatus();
        if (status == HttpStatus.SC_FORBIDDEN) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Code status : "
                            + status
                            + "Error body : "
                            + httpResponse.getBody(String.class));
        }
        return httpResponse;
    }
}
