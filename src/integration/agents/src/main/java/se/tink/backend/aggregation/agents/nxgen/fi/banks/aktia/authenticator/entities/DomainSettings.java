package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DomainSettings {

    @JsonProperty("pinTouchActivationAvailable")
    private boolean pinTouchActivationAvailable;

    @JsonProperty("pinTouchTermsPdfUrl")
    private String pinTouchTermsPdfUrl;

    @JsonProperty("ownTransferWithoutOtp")
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
