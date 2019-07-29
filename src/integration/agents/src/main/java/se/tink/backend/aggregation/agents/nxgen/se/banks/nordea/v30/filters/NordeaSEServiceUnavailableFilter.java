package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.filters;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class NordeaSEServiceUnavailableFilter extends Filter {
    private static final Logger log =
            LoggerFactory.getLogger(NordeaSEServiceUnavailableFilter.class);

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            log.error("Bank service unavailable, received Http Status code 503");
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        return response;
    }
}
