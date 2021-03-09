package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoginErrorResponseEntity {

    @JsonProperty("info-header")
    private String infoHeader;

    @JsonProperty("info-text")
    private String infoText;

    @JsonProperty("info-code")
    private String infoCode;

    @JsonProperty("support-id")
    private String supportId;
}
