package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestWrappingEntity {
    @JsonProperty("__type")
    private String type = "SEB_CS.SEBCSService";
    @JsonProperty("ServiceInput")
    private List<ServiceInput> serviceInput = Lists.newArrayList();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ServiceInput> getServiceInput() {
        return serviceInput;
    }

    public void setServiceInput(List<ServiceInput> serviceInput) {
        this.serviceInput = serviceInput;
    }
}
