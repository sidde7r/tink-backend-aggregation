package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private List<ErrorEntity> errors;

    @JsonIgnore
    public ErrorEntity getError() {
        if (errors == null || errors.isEmpty()) {
            return null;
        }

        // Don't know when there would be more than one error, picking first because we need to
        // pick something.
        return errors.get(0);
    }

    @JsonIgnore
    public boolean isInvalidDateError() {
        ErrorEntity error = getError();

        if (error == null) {
            return false;
        }

        return error.isInvalidDate();
    }
}
