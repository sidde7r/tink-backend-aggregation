package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity.FieldError;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ArgentaErrorResponse {
    private String code;
    private String message;
    private boolean reregister;
    private List<FieldError> fieldErrors;
}
