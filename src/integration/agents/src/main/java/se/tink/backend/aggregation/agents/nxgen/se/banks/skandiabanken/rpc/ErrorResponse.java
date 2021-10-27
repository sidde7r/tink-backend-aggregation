package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities.ErrorField;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Slf4j
public class ErrorResponse {
    private int statusCode;
    private String statusMessage = "";
    private String errorCode = "";
    private String errorMessage = "";
    private List<ErrorField> fields;

    @JsonIgnore
    public boolean isUnauthorized() {
        return statusMessage.equalsIgnoreCase("Unauthorized");
    }

    @JsonIgnore
    public boolean isInvalidPaymentDate() {
        return ErrorCodes.INVALID_PAYMENT_DATE.equalsIgnoreCase(errorCode);
    }

    @JsonIgnore
    public boolean isInvalidOcrError() {
        if (ListUtils.emptyIfNull(fields).size() != 1) {
            log.warn(
                    "List of fields did not contain exactly one field, can't determine error cause.");
            return false;
        }

        ErrorField errorField = fields.get(0);

        if (errorField.isOcrInvalidError()) {
            return true;
        }

        log.warn("Unknown error field: {}", errorField);
        return false;
    }
}
