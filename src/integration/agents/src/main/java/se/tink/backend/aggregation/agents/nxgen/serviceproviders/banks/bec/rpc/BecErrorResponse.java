package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BecErrorResponse {
    private String action;
    @Setter private String message;

    public boolean isKnownMessage() {
        return ErrorMessages.ERROR_MESSAGES_TO_REASON_MAP.keySet().stream()
                .anyMatch(errorMessage -> message.contains(errorMessage));
    }

    public String getReason() {
        Optional<String> key =
                ErrorMessages.ERROR_MESSAGES_TO_REASON_MAP.keySet().stream()
                        .filter(errorMessage -> message.contains(errorMessage))
                        .findAny();
        return key.map(ErrorMessages.ERROR_MESSAGES_TO_REASON_MAP::get).orElse("No reason found");
    }
}
