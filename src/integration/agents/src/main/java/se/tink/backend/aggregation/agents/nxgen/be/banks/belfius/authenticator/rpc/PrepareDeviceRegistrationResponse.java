package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Text;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareDeviceRegistrationResponse extends BelfiusResponse {

    public String getChallenge() {
        return ScreenUpdateResponse
                .findWidgetOrElseThrow(this,
                        BelfiusConstants.Widget.DEVICE_REGISTRATION_SIGNATURE_CHALLENGE)
                .getProperties(Text.class).getText();
    }
}
