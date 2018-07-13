package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DomainSettingsEntity {
    private boolean pinTouchActivationAvailable;
    private String pinTouchTermsPdfUrl;
    private boolean ownTransferWithoutOtp;

    public boolean isPinTouchActivationAvailable() {
        return pinTouchActivationAvailable;
    }

    public String getPinTouchTermsPdfUrl() {
        return pinTouchTermsPdfUrl;
    }

    public boolean isOwnTransferWithoutOtp() {
        return ownTransferWithoutOtp;
    }
}
