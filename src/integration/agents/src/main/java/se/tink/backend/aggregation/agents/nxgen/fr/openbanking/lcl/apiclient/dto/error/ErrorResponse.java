package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.error;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private String error;
    private String message;
    private String status;
    private String timestamp;
    private String path;
}
