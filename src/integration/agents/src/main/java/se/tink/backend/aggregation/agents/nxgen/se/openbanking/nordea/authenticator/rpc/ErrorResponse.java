package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.Error;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.FailuresItem;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.GroupHeader;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty("group_header")
    private GroupHeader groupHeader;

    @JsonProperty("_type")
    private String type;

    @JsonProperty("error")
    private Error error;

    public GroupHeader getGroupHeader() {
        return groupHeader;
    }

    public String getType() {
        return type;
    }

    public Error getError() {
        return error;
    }

    @JsonIgnore
    public boolean isSsnInvalidError() {
        if (error == null || error.getFailures().isEmpty()) {
            return false;
        }
        return error.getFailures().stream()
                .anyMatch(
                        failure ->
                                ErrorCode.VALIDATION_ERROR.equals(failure.getCode())
                                        && ErrorMessage.SSN_LENGTH_INCORRECT.equalsIgnoreCase(
                                                failure.getDescription())
                                        && ErrorMessage.PSU_ID.equalsIgnoreCase(failure.getPath())
                                        && ErrorMessage.PATTERN.equalsIgnoreCase(
                                                failure.getType()));
    }

    @JsonIgnore
    public boolean isKnownBankServiceError() {
        if (error == null || error.getFailures().isEmpty()) {
            return false;
        }

        return error.getFailures().stream()
                .anyMatch(
                        failure ->
                                ErrorCode.SERVER_ERROR.equalsIgnoreCase(failure.getCode())
                                        && ErrorMessage.UNEXPECTED_ERROR.equalsIgnoreCase(
                                                failure.getDescription()));
    }

    @JsonIgnore
    public Optional<FailuresItem> getFailure() {
        if (error == null || error.getFailures().isEmpty()) {
            return Optional.empty();
        } else {
            return error.getFailures().stream()
                    .filter(failuresItem -> failuresItem.getDescription() != null)
                    .findFirst();
        }
    }
}
