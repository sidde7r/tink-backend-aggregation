package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String errorDescription;
    private Integer httpStatus;
}
