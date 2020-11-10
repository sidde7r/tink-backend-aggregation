package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class BankErrorResponseFilter extends Filter {

    private final PersistentStorage persistentStorage;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {
            List<ErrorResponse> errorResponses = e.getResponse().getBody(List.class);

            if (errorResponses.stream().anyMatch(ErrorResponse::isConsentExpired)) {
                removeCredentialState();
                throw SessionError.CONSENT_EXPIRED.exception();
            } else if (errorResponses.stream().anyMatch(ErrorResponse::isConsentInvalid)) {
                removeCredentialState();
                throw SessionError.CONSENT_INVALID.exception();
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

    private void removeCredentialState() {
        persistentStorage.remove(Storage.CONSENT);
        persistentStorage.remove(Storage.OAUTH_TOKEN);
    }
}
