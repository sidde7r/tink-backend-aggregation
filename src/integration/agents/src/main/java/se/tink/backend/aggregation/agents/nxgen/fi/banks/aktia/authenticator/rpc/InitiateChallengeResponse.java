package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateChallengeResponse {

    private String phoneNumber;
    private String prefix;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPrefix() {
        return prefix;
    }
}
