package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankingServiceEntity {
    private ErrorMessage errorMessage;

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @JsonIgnore
    public Optional<String> getErrorCode() {
        return Optional.ofNullable(errorMessage != null ? errorMessage.getErrorCode() : null);
    }
}
