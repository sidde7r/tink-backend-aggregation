package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SantanderEsBankServiceExceptionFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpClientException e) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        }
    }
}
