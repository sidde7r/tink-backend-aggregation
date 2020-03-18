package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import java.util.List;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BankErrorResponseFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {
            List<ErrorResponse> errorResponses = e.getResponse().getBody(List.class);

            if (errorResponses.stream().anyMatch(ErrorResponse::isConsentExpired)) {
                throw BankServiceError.CONSENT_EXPIRED.exception();
            } else if (errorResponses.stream().anyMatch(ErrorResponse::isConsentInvalid)) {
                throw BankServiceError.CONSENT_INVALID.exception();
            } else if (errorResponses.stream().anyMatch(ErrorResponse::isServiceBlocked)) {
                final String message =
                        errorResponses.stream()
                                .filter(x -> x.getCode().contains(ErrorCodes.SERVICE_BLOCKED))
                                .findFirst()
                                .get()
                                .getText();
                throw BankServiceError.BANK_SIDE_FAILURE.exception(message);
            } else if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw e;
        }
    }
}
