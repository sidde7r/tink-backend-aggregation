package se.tink.backend.aggregation.agents.banks.seb.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {

    @JsonProperty("ResultInfo")
    public ResultInfo resultInfo;
    @JsonProperty("VODB")
    public VODB VODB;
    @JsonProperty("ServiceInput")
    public List<ServiceInput> ServiceInput = new ArrayList<ServiceInput>();
    @JsonProperty("UserCredentials")
    public UserCredentials UserCredentials;
    @JsonProperty("ServiceInfo")
    public String getServiceInfo() {
        return null;
    };

}
