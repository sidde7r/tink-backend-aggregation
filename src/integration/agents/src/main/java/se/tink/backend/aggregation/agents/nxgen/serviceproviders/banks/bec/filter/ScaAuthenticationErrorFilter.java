package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages.Authentication.INCORRECT_CREDENTIALS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages.Authentication.RESET_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages.FUNCTION_NOT_AVAILABLE;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.LoginErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ScaAuthenticationErrorFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == 400 && isRequestForScaAuthentication(httpRequest)) {

            LoginErrorResponse loginErrorResponse =
                    SerializationUtils.deserializeFromString(
                            response.getBody(String.class), LoginErrorResponse.class);

            String message =
                    Optional.ofNullable(loginErrorResponse)
                            .map(LoginErrorResponse::getMessage)
                            .orElse("Unknown error occurred.");

            handleScaAuthenticationError(message);
        }
        return response;
    }

    private void handleScaAuthenticationError(String message) {
        if (INCORRECT_CREDENTIALS.stream().anyMatch(message::contains)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (message.contains(RESET_TOKEN)) {
            throw SessionError.CONSENT_REVOKED.exception();
        } else if (FUNCTION_NOT_AVAILABLE.stream().anyMatch(message::contains)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(message);
        } else {
            throw new BecAuthenticationException(message);
        }
    }

    private boolean isRequestForScaAuthentication(HttpRequest httpRequest) {
        return httpRequest.getUrl().get().endsWith("/logon/SCAprepare")
                || httpRequest.getUrl().get().endsWith("/logon/SCA");
    }
}
