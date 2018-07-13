package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class LoginPinPad {

    /** 1-based indices of the digits that should be given */
    private List<Integer> pinPositions;
    private List<Integer> pinPadNumbers;
    private String lastConnection;
    private String lastDeviceConnection;
    private int numFails;
    private boolean registeredFingerprint;
    private List<String> pinpad;

    public List<Integer> getPinPositions() {
        return pinPositions;
    }

    public void setPinPositions(List<Integer> pinPositions) {
        this.pinPositions = pinPositions;
    }

    public List<Integer> getPinPadNumbers() {
        return pinPadNumbers;
    }

    public void setPinPadNumbers(List<Integer> pinPadNumbers) {
        this.pinPadNumbers = pinPadNumbers;
    }

    public String getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(String lastConnection) {
        this.lastConnection = lastConnection;
    }

    public String getLastDeviceConnection() {
        return lastDeviceConnection;
    }

    public void setLastDeviceConnection(String lastDeviceConnection) {
        this.lastDeviceConnection = lastDeviceConnection;
    }

    public int getNumFails() {
        return numFails;
    }

    public void setNumFails(int numFails) {
        this.numFails = numFails;
    }

    public boolean isRegisteredFingerprint() {
        return registeredFingerprint;
    }

    public void setRegisteredFingerprint(boolean registeredFingerprint) {
        this.registeredFingerprint = registeredFingerprint;
    }

    public List<String> getPinpad() {
        return pinpad;
    }

    public void setPinpad(List<String> pinpad) {
        this.pinpad = pinpad;
    }
}
