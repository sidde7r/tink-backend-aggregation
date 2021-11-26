package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.error.ErrorsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class EeOBaseEntity {

    @JsonProperty("codigoRetorno")
    private String returnCode;

    @JsonProperty("Errores")
    private ErrorsEntity errors;

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public void setErrors(ErrorsEntity errors) {
        this.errors = errors;
    }

    public boolean isUnsuccessfulReturnCode() {
        return returnCode.equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE);
    }

    public Optional<ErrorsEntity> getErrors() {
        return Optional.ofNullable(errors);
    }
}
