package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants.ErrorMessage;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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
            HttpResponse response, int errorStatus, String errorMessage, AgentError error) {
        if (response.getStatus() == errorStatus) {
            String body = response.getBody(String.class);
            if (body.contains(errorMessage)) {
                throw error.exception(
                        "Error status: " + errorStatus + ", message: " + errorMessage);
            }
        }
        // Not a match, do nothing
    }
}
