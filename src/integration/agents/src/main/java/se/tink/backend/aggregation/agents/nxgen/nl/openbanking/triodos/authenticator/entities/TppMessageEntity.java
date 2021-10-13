package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TppMessageEntity {
    private String text;
    private String code;
    private String category;

    @JsonIgnore
    public boolean isFormatError() {
        return ErrorCodes.FORMAT_ERROR.equalsIgnoreCase(code);
    }

    @JsonIgnore
    public boolean isInvalidProduct() {
        return ErrorCodes.PRODUCT_INVALID.equalsIgnoreCase(code);
    }

    @JsonIgnore
    public boolean isTokenInvalid() {
        return ErrorCodes.TOKEN_INVALID.equalsIgnoreCase(code);
    }

    @JsonIgnore
    public boolean isConsentInvalid() {
        return ErrorCodes.CONSENT_INVALID.equalsIgnoreCase(code);
    }
}
