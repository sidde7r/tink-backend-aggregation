package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PhoneNumberResponse {

    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
