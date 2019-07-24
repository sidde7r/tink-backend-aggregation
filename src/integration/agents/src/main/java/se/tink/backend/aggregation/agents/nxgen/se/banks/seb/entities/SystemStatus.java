package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemStatus {
    @JsonProperty private int systemcode;
    @JsonProperty private int errorcode;
    @JsonProperty private String systemtitle;
    @JsonProperty private String systemmessage;

    @JsonIgnore
    public int getSystemCode() {
        return systemcode;
    }

    @JsonIgnore
    public String getSystemMessage() {
        return systemmessage;
    }
}
