package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.dto.responses;

import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
public class ErrorResponse {
    private String message;
    private String code;
}
