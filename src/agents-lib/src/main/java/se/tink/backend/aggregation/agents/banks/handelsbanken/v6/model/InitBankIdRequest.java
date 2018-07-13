package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class InitBankIdRequest {
    private boolean authOtherDevice;
    private String personalNumber;

    public boolean isAuthOtherDevice() {
        return authOtherDevice;
    }

    public void setAuthOtherDevice(boolean authOtherDevice) {
        this.authOtherDevice = authOtherDevice;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

}
