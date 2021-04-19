package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private static final String UNKNOWN_ERROR = "Unknown error";
    private List<TppMessage> tppMessages;

    @JsonIgnore
    public boolean isBadRequest() {
        return containsError(ErrorCodes.FORMAT_ERROR);
    }

    @JsonIgnore
    public boolean hasInsufficientFunds() {
        return containsError(ErrorCodes.INSUFFICIENT_FUNDS);
    }

    @JsonIgnore
    public boolean isInvalidDebtor() {
        return containsError(ErrorCodes.INVALID_DEBTOR_ACCOUNT);
    }

    @JsonIgnore
    public boolean isInvalidCreditor() {
        return containsError(ErrorCodes.INVALID_DEBTOR_ACCOUNT);
    }

    @JsonIgnore
    private boolean containsError(String errorCode) {
        return getTppMessages().stream()
                .anyMatch(
                        tppMessage ->
                                errorCode.equalsIgnoreCase(tppMessage.getCode())
                                        || errorCode.equalsIgnoreCase(tppMessage.getText()));
    }

    @JsonIgnore
    public String getErrorMessage(String errorCode) {
        return getTppMessages().stream()
                .filter(tppMessage -> errorCode.equalsIgnoreCase(tppMessage.getCode()))
                .findFirst()
                .map(TppMessage::getText)
                .orElse(UNKNOWN_ERROR);
    }

    private List<TppMessage> getTppMessages() {
        return ofNullable(tppMessages).orElse(emptyList());
    }

    @JsonIgnore
    public String getErrorText() {
        return tppMessages.stream().map(TppMessage::getText).findFirst().orElse(UNKNOWN_ERROR);
    }
}
