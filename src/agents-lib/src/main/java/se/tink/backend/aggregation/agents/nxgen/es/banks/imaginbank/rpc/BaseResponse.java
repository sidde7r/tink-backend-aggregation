package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {
    @JsonProperty("codigo")
    private String code;
    @JsonProperty("mensaje")
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
