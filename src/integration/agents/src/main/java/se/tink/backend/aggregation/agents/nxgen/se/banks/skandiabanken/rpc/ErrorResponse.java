package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("StatusCode")
    private int statusCode;

    @JsonProperty("StatusMessage")
    private String statusMessage = "";

    @JsonProperty("ErrorCode")
    private String errorCode = "";

    @JsonProperty("ErrorMessage")
    private String errorMessage = "";

    @JsonIgnore
    public boolean isUnauthorized() {
        return statusMessage.equalsIgnoreCase("Unauthorized");
    }
}
