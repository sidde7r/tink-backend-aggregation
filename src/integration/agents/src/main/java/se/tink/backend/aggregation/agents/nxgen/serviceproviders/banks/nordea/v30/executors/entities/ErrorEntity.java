package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorEntity {
    @JsonProperty private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}
