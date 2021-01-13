package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankMethodNotAllowedFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
            String body = response.getBody(String.class);
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + response.getStatus() + " Error body: " + body);
        }

        return response;
    }

}
