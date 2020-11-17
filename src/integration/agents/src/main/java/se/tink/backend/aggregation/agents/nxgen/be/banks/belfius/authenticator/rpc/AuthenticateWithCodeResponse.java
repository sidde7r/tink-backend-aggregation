package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticateWithCodeResponse extends BelfiusResponse {

    public void validate() throws AuthenticationException {
        if (MessageResponse.isError(this)) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }
    }

    public boolean isChallengeResponseOk() {
        return !MessageResponse.isError(this);
    }
}
