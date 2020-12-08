package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankConsentLimitFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() == HttpStatus.ACCESS_EXCEEDED) {
            throw BankServiceError.ACCESS_EXCEEDED.exception();
        }
        return httpResponse;
    }
}
