package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentEntity {
    private String instrumentName;
    private boolean supported;
    private boolean disabledByPortal;
    private boolean mandatory;
    private boolean tp;

    public String getInstrumentName() {
        return instrumentName;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isDisabledByPortal() {
        return disabledByPortal;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isTp() {
        return tp;
    }
}
