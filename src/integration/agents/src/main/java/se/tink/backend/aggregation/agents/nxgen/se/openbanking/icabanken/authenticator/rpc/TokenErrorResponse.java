package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TokenErrorResponse {
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public boolean isInternalServerError() {
        return IcaBankenConstants.ErrorTypes.SERVER_ERROR.equalsIgnoreCase(error)
                && Optional.ofNullable(errorDescription)
                        .orElse("")
                        .toLowerCase()
                        .contains(IcaBankenConstants.ErrorMessages.UNEXPECTED_INTERNAL_EXCEPTION);
    }
}
