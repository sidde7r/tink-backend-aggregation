package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;
import java.util.Optional;

@JsonObject
public class BaseMobileResponseEntity {
    private HeaderEntity header;
    private String returnCode;
    private List<ErrorEntity> errors;

    public HeaderEntity getHeader() {
        return header;
    }

    public String getReturnCode() {
        if (Strings.isNullOrEmpty(returnCode)) {
            return "";
        }

        return returnCode;
    }

    public List<ErrorEntity> getErrors() {
        return errors;
    }

    @JsonIgnore
    public Optional<String> getErrorCode() {
        return errors != null &&  errors.get(0) != null ?
                Optional.ofNullable(errors.get(0).getCode()) : Optional.empty();
    }

    @JsonIgnore
    public Optional<String> getErrorText() {
        return errors != null &&  errors.get(0) != null ?
                Optional.ofNullable(errors.get(0).getText()) : Optional.empty();
    }

    private boolean isOutOfSession() {
        return IngConstants.ReturnCodes.NOK.equalsIgnoreCase(getReturnCode())
                && (getErrorCode().isPresent()
                        && IngConstants.ErrorCodes.OUT_OF_SESSIOM.equals(getErrorCode().get()))
                && (getErrorText().isPresent()
                        && IngConstants.ErrorText.OUT_OF_SESSIOM.equalsIgnoreCase(
                                getErrorText().get()));
    }

    public void validateSession() {
        if (isOutOfSession())
            throw new IllegalStateException("Out of session");
    }
}
