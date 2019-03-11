package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class EEBaseLoginResponse implements EERpcResponse {

    @Override
    public void handleReturnCode() throws LoginException {
        if (isUnsuccessfulReturnCode()) {
            switch (getErrors().get().getShowCode()) {
                case EvoBancoConstants.ErrorCodes.AUTHENTICATION_ERROR:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();

                default:
                    throw new IllegalStateException(
                            "Unknown unsuccessful return code " + getErrors().get().toString());
            }
        }
    }

}
