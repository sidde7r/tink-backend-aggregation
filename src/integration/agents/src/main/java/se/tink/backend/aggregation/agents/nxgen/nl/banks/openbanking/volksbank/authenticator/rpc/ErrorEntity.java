package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ErrorEntity {

    private String category;
    private String code;
    private String text;

    @JsonIgnore
    public boolean isConsentExpiredOrInvalid() {
        return ErrorCodes.CONSENT_STATUS.contains(code);
    }

    @JsonIgnore
    public boolean isServiceBlocked() {
        return code.equalsIgnoreCase(ErrorCodes.SERVICE_BLOCKED);
    }
}
