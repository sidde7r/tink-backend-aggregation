package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HWINFO01 {
    
    @JsonProperty("LONGITUDE_DECIMAL")
    public String LONGITUDE_DECIMAL = "0";
    @JsonProperty("LATITUDE_DECIMAL")
    public String LATITUDE_DECIMAL = "0";
    @JsonProperty("COUNTRY_PREFIX")
    public int COUNTRY_PREFIX = 0;


}
