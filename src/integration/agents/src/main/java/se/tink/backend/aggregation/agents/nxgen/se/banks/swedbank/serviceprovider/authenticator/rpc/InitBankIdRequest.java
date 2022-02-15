package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

/**
 * Request for initiating bankID for login. When bankIdOnSameDevice is set to true, we get an
 * autostart token in return. When set to false Swedbank returns a link to a QR code image that we
 * need to parse into an autostart token.
 */
@Getter
@JsonObject
public class InitBankIdRequest {
    private boolean bankIdOnSameDevice;

    private InitBankIdRequest(boolean bankIdOnSameDevice) {
        this.bankIdOnSameDevice = bankIdOnSameDevice;
    }

    @JsonIgnore
    public static InitBankIdRequest create(boolean bankIdOnSameDevice) {
        return new InitBankIdRequest(bankIdOnSameDevice);
    }
}
