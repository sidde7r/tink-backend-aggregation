package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdRequest {
    private boolean authOtherDevice;
    private String personalNumber;

    public InitBankIdRequest setAuthOtherDevice(boolean authOtherDevice) {
        this.authOtherDevice = authOtherDevice;
        return this;
    }

    public InitBankIdRequest setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
        return this;
    }
}
