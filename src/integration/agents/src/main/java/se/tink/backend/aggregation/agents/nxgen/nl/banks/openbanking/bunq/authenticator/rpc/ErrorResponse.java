package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("Error")
    private List<ErrorEntity> errors;

    public boolean isIncorrectApiKeyOrIpAddressError() {
        if (errors.size() != 1) {
            // More than one error in error list could imply other issue
            return false;
        }

        // This will be used to decide if we should throw SESSION_EXPIRED. Comparing against the
        // exact error description since it's very important that we don't get it wrong.
        return "User credentials are incorrect. Incorrect API key or IP address."
                .equalsIgnoreCase(errors.get(0).getErrorDescription());
    }
}
