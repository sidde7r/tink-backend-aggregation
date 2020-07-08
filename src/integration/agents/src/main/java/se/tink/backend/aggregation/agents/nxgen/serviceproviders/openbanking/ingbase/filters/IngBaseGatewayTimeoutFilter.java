package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class IngBaseGatewayTimeoutFilter extends Filter {
    private static final Logger log = LoggerFactory.getLogger(IngBaseGatewayTimeoutFilter.class);

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT
                || response.getStatus() == HttpStatus.SC_BAD_GATEWAY) {
            log.warn(
                    "Bank service unavailable, received Http Status code %s", response.getStatus());
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        return response;
    }
}
