package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.ConsentErrors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TppMessageEntity {
    private String text;
    private String code;
    private String category;

    public boolean isFormatError() {
        return ConsentErrors.FORMAT_ERROR.equalsIgnoreCase(code);
    }

    public boolean isInvalidProduct() {
        return ConsentErrors.PRODUCT_INVALID.equalsIgnoreCase(code);
    }
}
