package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ErrorField {
    private String field;
    private String message;

    public boolean isOcrInvalidError() {
        return ErrorMessages.INVALID_OCR.equalsIgnoreCase(field);
    }
}
