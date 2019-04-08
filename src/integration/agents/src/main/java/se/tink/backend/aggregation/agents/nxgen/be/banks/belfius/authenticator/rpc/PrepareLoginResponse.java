package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Text;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareLoginResponse extends BelfiusResponse {

    public String getChallenge() {
        return ScreenUpdateResponse.findWidgetOrElseThrow(
                        this, BelfiusConstants.Widget.LOGIN_PW_CHALLENGE)
                .getProperties(Text.class)
                .getText();
    }

    public String getContractNumber() {
        return ScreenUpdateResponse.findWidgetOrElseThrow(
                        this, BelfiusConstants.Widget.LOGIN_PW_CONTRACT)
                .getProperties(Text.class)
                .getText();
    }

    public void validate() throws LoginException {
        MessageResponse messageResponse =
                this.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse != null
                && messageResponse
                        .getMessageType()
                        .equalsIgnoreCase(BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE)) {
            if (StringUtils.containsIgnoreCase(
                    messageResponse.getMessageDetail(),
                    BelfiusConstants.ErrorCodes.MISSING_MOBILEBANKING_SUBSCRIPTION)) {
                throw LoginError.REGISTER_DEVICE_ERROR.exception();
            }
        }
    }
}
