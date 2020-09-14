package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

/**
 * Request for initiating bankID for login. bankIdOnSameDevice is always set to true to get an
 * autostart token in return. When set to false Swedbank returns a link to a QR code image that we
 * need to parse into an autostart token.
 */
@Getter
@JsonObject
public class InitBankIdRequest {
    private boolean bankIdOnSameDevice;

    private InitBankIdRequest() {
        this.bankIdOnSameDevice = true;
    }

    @JsonIgnore
    public static InitBankIdRequest create() {
        return new InitBankIdRequest();
    }
}
