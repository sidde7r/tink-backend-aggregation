package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpInfoEntity {
    private String nextOtpIndex;
    private String currentOtpCard;
    private boolean fixedOtpCard;
    private String nextOtpCard;

    public String getNextOtpIndex() {
        return nextOtpIndex;
    }

    public String getCurrentOtpCard() {
        return currentOtpCard;
    }

    public boolean isFixedOtpCard() {
        return fixedOtpCard;
    }

    public String getNextOtpCard() {
        return nextOtpCard;
    }
}
