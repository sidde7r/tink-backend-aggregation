package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private static Logger log = LoggerFactory.getLogger(ErrorResponse.class);

    private List<ErrorEntity> errors;

    @JsonIgnore
    private List<ErrorEntity> getErrorsAndLogIfMultiple() {
        if (errors == null) {
            return Collections.emptyList();
        }

        if (errors.size() > 1) {
            log.warn(
                    "Received multiple errors which is not expected, consult debug logs to investigate.");
        }

        return errors;
    }

    @JsonIgnore
    public boolean isInvalidDateError() {
        return getErrorsAndLogIfMultiple().stream().anyMatch(ErrorEntity::isInvalidDate);
    }

    @JsonIgnore
    public boolean isFailedSignature() {
        return getErrorsAndLogIfMultiple().stream().anyMatch(ErrorEntity::isFailedSignature);
    }

    @JsonIgnore
    public boolean isAmountLimitReached() {
        return getErrorsAndLogIfMultiple().stream().anyMatch(ErrorEntity::isAmountLimitReached);
    }

    @JsonIgnore
    public boolean isAmountExceedsCurrentBalance() {
        return getErrorsAndLogIfMultiple().stream()
                .anyMatch(ErrorEntity::isAmountExceedsCurrentBalance);
    }
}
