package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginErrorResponseEntity {

    @JsonProperty("info-header")
    private String infoHeader;

    @JsonProperty("info-text")
    private String infoText;

    @JsonProperty("info-code")
    private String infoCode;

    @JsonProperty("support-id")
    private String supportId;

    public String getInfoHeader() {
        return infoHeader;
    }

    public String getInfoText() {
        return infoText;
    }

    public String getInfoCode() {
        return infoCode;
    }

    public String getSupportId() {
        return supportId;
    }
}
