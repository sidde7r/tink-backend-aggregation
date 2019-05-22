package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OtpInfo {

    @JsonProperty("fixedOtpCard")
    private boolean fixedOtpCard;

    @JsonProperty("nextOtpCard")
    private Object nextOtpCard;

    @JsonProperty("currentOtpCard")
    private String currentOtpCard;

    @JsonProperty("nextOtpIndex")
    private String nextOtpIndex;

    public boolean isFixedOtpCard() {
        return fixedOtpCard;
    }

    public Object getNextOtpCard() {
        return nextOtpCard;
    }

    public String getCurrentOtpCard() {
        return currentOtpCard;
    }

    public String getNextOtpIndex() {
        return nextOtpIndex;
    }
}
