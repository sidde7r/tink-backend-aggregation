package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.executor.beneficiary.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorsItem {
    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;
}
