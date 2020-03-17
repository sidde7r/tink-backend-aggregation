package se.tink.backend.aggregation.agents.nxgen.se.business.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
