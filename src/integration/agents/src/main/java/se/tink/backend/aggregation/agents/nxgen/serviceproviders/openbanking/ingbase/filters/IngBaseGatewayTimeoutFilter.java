package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class IngBaseGatewayTimeoutFilter extends Filter {
    private static final Logger log = LoggerFactory.getLogger(IngBaseGatewayTimeoutFilter.class);

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT) {
            log.warn("Bank service unavailable, received Http Status code 504");
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        return response;
    }
}
