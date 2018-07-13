package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

public class PinLogonRequest {
    private String userId;
    private String pin;

    public String getUserId() {
        return userId;
    }

    public PinLogonRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getPin() {
        return pin;
    }

    public PinLogonRequest setPin(String pin) {
        this.pin = pin;
        return this;
    }
}
