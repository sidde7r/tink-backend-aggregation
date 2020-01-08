package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaSeFilter extends Filter {

    private static final int TOO_MANY_REQUEST_HTTP_STATUS = 429;

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
        if (response.getStatus() == TOO_MANY_REQUEST_HTTP_STATUS) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        return response;
    }
}
