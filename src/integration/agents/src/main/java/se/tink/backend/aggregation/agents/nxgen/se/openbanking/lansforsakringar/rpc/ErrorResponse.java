package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonObject
public class ErrorResponse {
    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}
