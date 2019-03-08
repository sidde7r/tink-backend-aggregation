package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Optional;

@JsonObject
public abstract class EeOBaseEntity {

    @JsonProperty("codigoRetorno")
    private String returnCode;

    @JsonProperty("Errores")
    private ErrorsEntity errors;

    public String getReturnCode() {
        return returnCode;
    }

    public boolean isUnsuccessfulReturnCode() {
        return returnCode.equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE);
    }

    public Optional<ErrorsEntity> getErrors() {
        if (errors != null) {
            return Optional.of(errors);
        }
        return Optional.empty();
    }
}
