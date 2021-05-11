package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Response {

    private String code;
    private String message;

    public boolean hasCode(String code) {
        return this.code.equals(code);
    }
}
