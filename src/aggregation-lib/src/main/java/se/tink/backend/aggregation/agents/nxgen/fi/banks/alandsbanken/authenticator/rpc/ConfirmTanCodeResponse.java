package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;

public class ConfirmTanCodeResponse extends AlandsBankenResponse {

    private boolean pinChangeNeeded;
    private String sessionKey;

    public boolean isPinChangeNeeded() {
        return pinChangeNeeded;
    }

    public void setPinChangeNeeded(boolean pinChangeNeeded) {
        this.pinChangeNeeded = pinChangeNeeded;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
