package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class Payload {

    @JsonProperty("ResultInfo")
    private ResultInfo resultInfo;

    @JsonProperty("VODB")
    private VODB vodb;

    @JsonProperty("__type")
    private String type = "SEB_CS.SEBCSService";

    @JsonProperty("ServiceInput")
    private List<ServiceInput> serviceInput = new ArrayList<>();

    @JsonProperty("UserCredentials")
    private UserCredentials userCredentials;

    @JsonProperty("ServiceInfo")
    private String getServiceInfo() {
        return null;
    }
}
