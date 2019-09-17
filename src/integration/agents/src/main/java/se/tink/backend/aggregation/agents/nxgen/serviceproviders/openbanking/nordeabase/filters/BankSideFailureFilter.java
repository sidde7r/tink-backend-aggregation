package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters;

import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

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
