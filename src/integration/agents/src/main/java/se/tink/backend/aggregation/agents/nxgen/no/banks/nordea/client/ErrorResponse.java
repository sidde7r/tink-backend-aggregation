package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class ErrorResponse {
    private String error;
    private String errorDescription;
    private Integer httpStatus;
}
