package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InitSignRequest {
    @JsonProperty("Type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static InitSignRequest bundled() {
        InitSignRequest request = new InitSignRequest();
        request.setType("Bundle");

        return request;
    }
}
