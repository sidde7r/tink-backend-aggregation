package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Strings;
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
                        this, BelfiusConstants.Widget.LOGIN_SOFT_CHALLENGE)
                .getProperties(Text.class)
                .getText();
    }

    public String getContractNumber() {
        return ScreenUpdateResponse.findWidgets(
                        this, BelfiusConstants.Widget.LOGON_SOFT_CONTRACT_NUMBER)
                .stream()
                .filter(w -> w.getProperties(Text.class) != null)
                .map(w -> w.getProperties(Text.class))
                .filter(p -> !Strings.isNullOrEmpty(p.getText()))
                .map(Text::getText)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find contract number"));
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

    public boolean isDeviceRegistrationError() {
        MessageResponse messageResponse =
                this.filter(MessageResponse.class).findFirst().orElse(null);
        return messageResponse != null
                && messageResponse
                        .getMessageType()
                        .equalsIgnoreCase(BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE)
                && StringUtils.containsIgnoreCase(
                        messageResponse.getMessageDetail(),
                        BelfiusConstants.ErrorCodes.MISSING_MOBILEBANKING_SUBSCRIPTION);
    }
}
