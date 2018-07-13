package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemStatus {
    public int systemcode;
    public int errorcode;
    public String systemtitle;
    public String systemmessage;
}
