package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tesco.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Setter
@Getter
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Response {

    private String code;
    private String id;
    private String message;

    public boolean hasCode(String code) {
        return this.code.equals(code);
    }
}
