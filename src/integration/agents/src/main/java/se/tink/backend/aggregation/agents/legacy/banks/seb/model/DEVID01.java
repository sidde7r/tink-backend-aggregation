package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DEVID01 {

    @JsonProperty("APPLICATION_VERSION")
    public String APPLICATION_VERSION = "9.1.1";
    @JsonProperty("OS_NAME")
    public String OS_NAME = "iOS";
    @JsonProperty("MODEL")
    public String MODEL = "API_VERSION=2";
    @JsonProperty("MANUFACTURER")
    public String MANUFACTURER = "Apple";
    @JsonProperty("OS_VERSION")
    public String OS_VERSION = "12.0.1";
    @JsonProperty("APPLICATION_NAME")
    public String APPLICATION_NAME = "MASP";

    
}
