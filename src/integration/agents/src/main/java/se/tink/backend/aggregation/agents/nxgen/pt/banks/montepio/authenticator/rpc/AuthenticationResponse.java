package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.GenericResponse;

public class AuthenticationResponse extends GenericResponse {

    public boolean isWrongCredentials() {
        return error != null
                && (MontepioConstants.ErrorMessages.INVALID_LOGIN.equals(error.getCode())
                        || MontepioConstants.ErrorMessages.INVALID_PASSWORD.equals(
                                error.getCode()));
    }
}
