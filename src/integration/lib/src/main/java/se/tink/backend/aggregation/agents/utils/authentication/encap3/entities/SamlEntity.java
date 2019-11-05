package se.tink.backend.aggregation.agents.utils.authentication.encap3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SamlEntity {
    @JsonProperty private String contextContent;
    @JsonProperty private Object plugin;
    @JsonProperty private int responseType;

    @JsonIgnore
    public int getResponseType() {
        return responseType;
    }

    @JsonIgnore
    public String getContextContent() {
        return contextContent;
    }

    @JsonIgnore
    public String getSamlObjectAsBase64() {
        return contextContent;
    }
}
