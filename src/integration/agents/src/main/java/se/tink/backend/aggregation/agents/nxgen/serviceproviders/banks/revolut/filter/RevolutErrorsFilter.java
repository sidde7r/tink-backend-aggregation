package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentRuntimeError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants.ErrorMessage;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

/**
 * Revolut API fails for a wide range of uncommon errors. This filter is targeting the more obvious
 * ones.
 */
public class RevolutErrorsFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        // <h1>Bad Message 400</h1><pre>reason: Illegal character CNTL=0x0</pre>
        conditionalThrow(
                response,
                HttpStatus.SC_BAD_REQUEST,
                ErrorMessage.ILLEGAL_CHARACTER,
                BankServiceError.BANK_SIDE_FAILURE);

        // {"message":"HikariPool-3 - Connection is not available, request timed out after 5483ms."}
        conditionalThrow(
                response,
                HttpStatus.SC_INTERNAL_SERVER_ERROR,
                ErrorMessage.HIKARIPOOL,
                BankServiceError.BANK_SIDE_FAILURE);

        // <html>...<h2>The server encountered a temporary error and could not complete your
        // request.<p>Please try again in 30 seconds.</h2>...</html>
        conditionalThrow(
                response,
                HttpStatus.SC_BAD_GATEWAY,
                ErrorMessage.TEMPORARY_ERROR,
                BankServiceError.BANK_SIDE_FAILURE);

        return response;
    }

    private void conditionalThrow(
            HttpResponse response, int errorStatus, String errorMessage, AgentRuntimeError error) {
        if (response.getStatus() == errorStatus) {
            String body = response.getBody(String.class);
            if (body.contains(errorMessage)) {
                throw error.exception();
            }
        }
        // Not a match, do nothing
    }
}
