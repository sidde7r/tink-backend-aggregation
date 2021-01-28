package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.ConsentErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ValidateConsentErrorResponse {
    private String detail;
    private String type;
    private ValidateConsentUserMessageErrorResponse userMessage;
    private String error;

    @JsonIgnore
    public boolean isLoginExpired() {
        return ConsentErrorMessages.INVALID_TOKEN.equals(error)
                && ConsentErrorMessages.LOGIN_TIMEOUT.equals(userMessage.getTitle());
    }
}
