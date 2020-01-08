package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants.VerificationCodeChannel;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerificationChannelEntity {

    private String channel;
    private String value;

    public boolean isCallChannel() {
        return VerificationCodeChannel.CALL.equalsIgnoreCase(channel);
    }
}
