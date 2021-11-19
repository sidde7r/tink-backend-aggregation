package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class ErrorResponse {
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String BLOCKED_OR_NON_EXISTENT =
            "Compte inexistant ou bloque, Veuillez contacter votre conseiller";
    private static final String NO_ACCOUNTS_AVAILABLE = "Not found, no account available";

    private String error;
    private String errorDescription;

    public boolean isBlockedOrNonExistent() {
        return INVALID_REQUEST.equals(error) && BLOCKED_OR_NON_EXISTENT.equals(errorDescription);
    }

    public boolean isNoAccountsAvailable() {
        return NO_ACCOUNTS_AVAILABLE.equals(error);
    }
}
