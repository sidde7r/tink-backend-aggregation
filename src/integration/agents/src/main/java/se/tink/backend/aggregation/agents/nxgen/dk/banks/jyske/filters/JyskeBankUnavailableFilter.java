package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.filters;

import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

/**
 * During certain times, the bank sends the following exception
 *
 * <p>Response statusCode: 400 with body: {"errorCode":150,"errorMessage":"mobilbanken er lukket
 * hverdage og lørdag mellem 03:00 og 05:00 og søndag mellem 02:00 og
 * 06:00.","debugMessage":"Server: pfm1","status":"BAD_REQUEST","suppressed":[]}
 *
 * <p>This filter is implemented to handle this error message properly
 */
public class JyskeBankUnavailableFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == 400) {
            String body = response.getBody(String.class).toLowerCase();
            if (body.contains(ErrorMessages.BANK_UNAVAILABLE_DURING_MIDNIGHT)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
        }

        return response;
    }
}
