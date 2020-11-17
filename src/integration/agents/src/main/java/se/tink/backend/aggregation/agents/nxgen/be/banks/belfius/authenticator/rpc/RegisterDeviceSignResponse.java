package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.UnknownDeviceRegistrationError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agentsplatform.framework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.framework.error.IncorrectCardReaderResponseCodeError;

public class RegisterDeviceSignResponse extends BelfiusResponse {
    public void validate() throws AuthenticationException {
        MessageResponse messageResponse =
                this.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse != null
                && messageResponse
                        .getMessageType()
                        .equalsIgnoreCase(BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE)) {
            if ((StringUtils.containsIgnoreCase(
                            messageResponse.getMessageDetail(),
                            BelfiusConstants.ErrorCodes.ERROR_SIGN_CODE))
                    || StringUtils.containsIgnoreCase(
                            messageResponse.getMessageDetail(),
                            BelfiusConstants.ErrorCodes.ERROR_EMPTY_SIGN_CODE)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            } else if (StringUtils.containsIgnoreCase(
                    messageResponse.getMessageDetail(),
                    BelfiusConstants.ErrorCodes.DEVICE_REGISTRATION_ERROR)) {
                throw new IllegalStateException("Unknown device registration error");
            }
        }
    }

    public Optional<AgentBankApiError> checkForErrors() {
        MessageResponse messageResponse =
                this.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse != null
                && messageResponse
                        .getMessageType()
                        .equalsIgnoreCase(BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE)) {
            if ((StringUtils.containsIgnoreCase(
                            messageResponse.getMessageDetail(),
                            BelfiusConstants.ErrorCodes.ERROR_SIGN_CODE))
                    || StringUtils.containsIgnoreCase(
                            messageResponse.getMessageDetail(),
                            BelfiusConstants.ErrorCodes.ERROR_EMPTY_SIGN_CODE)) {
                return Optional.of(new IncorrectCardReaderResponseCodeError());
            } else if (StringUtils.containsIgnoreCase(
                    messageResponse.getMessageDetail(),
                    BelfiusConstants.ErrorCodes.DEVICE_REGISTRATION_ERROR)) {
                return Optional.of(new UnknownDeviceRegistrationError());
            }
        }
        return Optional.empty();
    }
}
