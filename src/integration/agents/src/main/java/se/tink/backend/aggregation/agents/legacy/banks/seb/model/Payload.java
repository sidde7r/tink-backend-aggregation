package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Payload {

    @JsonProperty("ResultInfo")
    public ResultInfo resultInfo;

    @JsonProperty("VODB")
    public VODB VODB;

    @JsonProperty("__type")
    public String type = "SEB_CS.SEBCSService";

    @JsonProperty("ServiceInput")
    public List<ServiceInput> ServiceInput = new ArrayList<ServiceInput>();

    @JsonProperty("UserCredentials")
    public UserCredentials UserCredentials;

    @JsonProperty("ServiceInfo")
    public String getServiceInfo() {
        return null;
    };
}
