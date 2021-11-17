package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ConsentStatus.VALID;

import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ConsentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentStatusResponse {
    private String consentStatus;

    public boolean isConsentValid() {
        return VALID.equalsIgnoreCase(consentStatus);
    }

    public void checkConsentRejected() throws LoginException {
        if (consentStatus.equalsIgnoreCase(ConsentStatus.REJECTED)) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
    }
}
