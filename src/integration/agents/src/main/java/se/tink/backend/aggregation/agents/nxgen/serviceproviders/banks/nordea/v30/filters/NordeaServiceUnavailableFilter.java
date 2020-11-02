package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.filters;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaServiceUnavailableFilter extends Filter {
    private static final Logger log = LoggerFactory.getLogger(NordeaServiceUnavailableFilter.class);

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.NO_BANK_SERVICE.exception(
                    "Bank service unavailable, received Http Status code 503");
        }
        return response;
    }
}
