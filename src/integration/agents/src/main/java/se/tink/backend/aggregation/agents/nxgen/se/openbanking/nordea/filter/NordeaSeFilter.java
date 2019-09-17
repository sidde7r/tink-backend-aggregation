package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class NordeaSeFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isKnownBankServiceError()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }

        return response;
    }
}
