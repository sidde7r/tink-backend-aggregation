package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;

public class ConfirmTanCodeResponse extends CrossKeyResponse {

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
