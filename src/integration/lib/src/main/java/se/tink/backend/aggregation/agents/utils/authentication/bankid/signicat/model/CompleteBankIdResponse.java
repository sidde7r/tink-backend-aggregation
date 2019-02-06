package se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteBankIdResponse {
    @JsonProperty("SAMLResponse")
    private String samlResponse;

    public String getSamlResponse() {
        return samlResponse;
    }

    public void setSamlResponse(String samlResponse) {
        this.samlResponse = samlResponse;
    }

    private String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
