package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity.FieldError;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ArgentaErrorResponse {
    String code;
    String message;
    boolean reregister;

    @JsonProperty("fieldErrors")
    List<FieldError> fieldErrorList;

    public String getCode() {
        return code;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrorList;
    }

    public String getMessage() {
        return message;
    }
}
