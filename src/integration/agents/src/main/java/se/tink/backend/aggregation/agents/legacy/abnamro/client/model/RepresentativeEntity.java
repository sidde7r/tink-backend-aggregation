package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepresentativeEntity {

    @JsonProperty("class")
    private String type;
    private String reference;
    
    public String getType() {
        return type;
    }
    public String getReference() {
        return reference;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
}
