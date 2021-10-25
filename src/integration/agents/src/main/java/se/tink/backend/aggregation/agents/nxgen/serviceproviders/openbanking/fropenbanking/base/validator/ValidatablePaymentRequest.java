package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

public interface ValidatablePaymentRequest {

    String INST = "INST";

    String getLocalInstrument();

    LocalDate getRequestedExecutionDateAsLocalDate();

    @JsonIgnore
    default boolean isInstantPaymentRequest() {
        return INST.equals(getLocalInstrument());
    }
}
