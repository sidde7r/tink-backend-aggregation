package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdRequest {

    @JsonProperty("SEB_Referer")
    private String sebReferer;

    public InitiateBankIdRequest(String sebReferer) {
        this.sebReferer = sebReferer;
    }
}
