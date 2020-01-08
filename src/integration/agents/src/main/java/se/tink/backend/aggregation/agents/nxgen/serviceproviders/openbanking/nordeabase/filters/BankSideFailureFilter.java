package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BankSideFailureFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        try {

            return nextFilter(httpRequest);

        } catch (HttpResponseException e) {

            if (e.getResponse().hasBody()) {
                if (e.getResponse().getBody(NordeaErrorResponse.class).isBankSideFailure()) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                }
            }

            throw e;
        }
    }
}
