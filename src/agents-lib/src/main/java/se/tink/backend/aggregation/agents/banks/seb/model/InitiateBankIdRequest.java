package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdRequest {
    private String uid;
    @JsonProperty("SEB_Referer")
    private String sebReferer;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSebReferer() {
        return sebReferer;
    }

    public void setSebReferer(String sebReferer) {
        this.sebReferer = sebReferer;
    }
}
