package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdRequest {

    private String bidDevice;

    public InitBankIdRequest setBidDevice(String bidDevice) {
        this.bidDevice = bidDevice;
        return this;
    }
}
