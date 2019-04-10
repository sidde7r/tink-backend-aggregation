package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentInfoEntity {
    private List<InstrumentEntity> instruments;
    private String userType;
    private String previousUserid;
    private String previousAuthMethod;
    private String previousMobilePhonenumber;
    private boolean bid20;
    private boolean bidPrecheckDisabled;

    public List<InstrumentEntity> getInstruments() {
        return instruments;
    }

    public String getUserType() {
        return userType;
    }

    public String getPreviousUserid() {
        return previousUserid;
    }

    public String getPreviousAuthMethod() {
        return previousAuthMethod;
    }

    public String getPreviousMobilePhonenumber() {
        return previousMobilePhonenumber;
    }

    public boolean isBid20() {
        return bid20;
    }

    public boolean isBidPrecheckDisabled() {
        return bidPrecheckDisabled;
    }
}
