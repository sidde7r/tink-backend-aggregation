package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.authenticator.entities.lightLogin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.entities.ErrorMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationServiceFaultEntity {
    private ErrorMessage errorMessage;

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @JsonIgnore
    public Optional<String> getErrorCode() {
        return Optional.ofNullable(errorMessage).map(ErrorMessage::getErrorCode);
    }
}
