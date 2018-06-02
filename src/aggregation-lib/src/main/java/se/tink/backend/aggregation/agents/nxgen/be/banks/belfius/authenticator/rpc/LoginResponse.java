package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BelfiusResponse {
    public void validate() throws AuthenticationException {
        MessageResponse messageResponse = this.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse != null
                && messageResponse.getMessageType().equalsIgnoreCase(BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE)
                && StringUtils.containsIgnoreCase(messageResponse.getMessageDetail(), BelfiusConstants.ErrorCodes.WRONG_CREDENTIALS_CODE)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
