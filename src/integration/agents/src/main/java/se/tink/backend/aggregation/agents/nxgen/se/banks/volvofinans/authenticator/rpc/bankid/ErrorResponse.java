package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ErrorResponse {
    private String message;

    @JsonIgnore
    public boolean isBankIdAlreadyInProgressError() {
        return !Strings.isNullOrEmpty(message)
                && message.contains(VolvoFinansConstants.Message.ALREADY_IN_PROGRESS);
    }
}
