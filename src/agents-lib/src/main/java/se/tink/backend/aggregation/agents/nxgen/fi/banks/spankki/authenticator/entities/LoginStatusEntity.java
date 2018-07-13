package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginStatusEntity {
    private String passwordStatus;
    private String passwordStatusMessage;
    private String pinPosition;
    private String pinStatus;
    private String pinStatusMessage;

    public String getPasswordStatus() {
        return passwordStatus;
    }

    public String getPasswordStatusMessage() {
        return passwordStatusMessage;
    }

    public String getPinPosition() {
        return pinPosition;
    }

    public String getPinStatus() {
        return pinStatus;
    }

    public String getPinStatusMessage() {
        return pinStatusMessage;
    }
}
