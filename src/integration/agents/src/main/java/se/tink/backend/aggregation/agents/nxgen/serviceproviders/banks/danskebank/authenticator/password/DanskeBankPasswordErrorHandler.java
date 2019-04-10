package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class DanskeBankPasswordErrorHandler {

    static void throwError(HttpResponseException hre)
            throws AuthenticationException, AuthorizationException {
        HttpResponse response = hre.getResponse();
        if (response.getStatus() >= 500) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        FinalizeAuthenticationResponse finalizeAuthenticationResponse =
                DanskeBankDeserializer.convertStringToObject(
                        response.getBody(String.class), FinalizeAuthenticationResponse.class);
        switch (finalizeAuthenticationResponse.getSessionStatus()) {
            case 519:
                throw AuthorizationError.UNAUTHORIZED.exception();
            case 520:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            default:
                throw hre;
        }
    }
}
