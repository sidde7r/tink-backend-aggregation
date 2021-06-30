package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.filter;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class LunarNotFoundFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == 404) {
            log.error("{} Throwing BankServiceException on 404 response", LUNAR_TAG);
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return response;
    }
}
