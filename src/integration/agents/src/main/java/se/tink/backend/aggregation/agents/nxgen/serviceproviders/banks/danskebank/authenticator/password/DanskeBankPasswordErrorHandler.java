package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DanskeBankPasswordErrorHandler {

    public static void throwError(HttpResponseException hre)
            throws AuthenticationException, AuthorizationException {
        HttpResponse response = hre.getResponse();
        if (response.getStatus() == 503) {
            throw hre;
        }
        FinalizeAuthenticationResponse finalizeAuthenticationResponse =
                DanskeBankDeserializer.convertStringToObject(
                        response.getBody(String.class), FinalizeAuthenticationResponse.class);
        switch (finalizeAuthenticationResponse.getSessionStatus()) {
            case 519:
                throw AuthorizationError.UNAUTHORIZED.exception(hre);
            case 520:
                throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
            default:
                if (response.getStatus() == 401) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
                }
                throw hre;
        }
    }
}
