package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class ErrorResponse {
    // Observed values: "invalid_grant"
    private String error;
    // Observed values: "Dieser Benutzer wird endg?ltig gesperrt weil ausser Gebrauch."
    private String error_description;

    public boolean isPermanentlyBlocked() {
        final String message = AxaConstants.Response.BLOCKED_SUBSTRING;
        return Optional.ofNullable(error_description)
                .filter(s -> StringUtils.containsIgnoreCase(s, message))
                .isPresent();
    }
}
