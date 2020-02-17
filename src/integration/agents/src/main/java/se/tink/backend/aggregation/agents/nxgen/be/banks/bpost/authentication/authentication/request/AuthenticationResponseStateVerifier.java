package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.AuthenticationResponseState;

public class AuthenticationResponseStateVerifier {

    public static <T extends AuthenticationResponseState> T checkIsNotError(T responseState)
            throws RequestException {
        if (responseState.isError()) {
            throw new RequestException(LoginError.INCORRECT_CHALLENGE_RESPONSE, "");
        }
        return responseState;
    }
}
