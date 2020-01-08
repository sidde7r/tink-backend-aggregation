package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants.VerificationCodeChannel;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignInResponse {

    private String channel;

    public boolean isSmsChannel() {
        return VerificationCodeChannel.SMS.equalsIgnoreCase(channel);
    }
}
