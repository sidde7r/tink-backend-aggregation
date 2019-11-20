package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

/** ordered before {@link NordeaHttpRetryFilter} so exceptions are only throw after retrying */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = 1)
public class NordeaServiceUnavailableFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (HttpStatus.SC_BAD_GATEWAY == response.getStatus()
                || HttpStatus.SC_SERVICE_UNAVAILABLE == response.getStatus()) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        return response;
    }
}
