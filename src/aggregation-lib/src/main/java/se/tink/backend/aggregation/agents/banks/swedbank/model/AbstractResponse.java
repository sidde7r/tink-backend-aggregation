package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractResponse {
    protected ErrorMessages errorMessages;

    public ErrorMessages getErrorMessages() {
        return errorMessages;
    }

    public boolean hasGeneralErrorWithCode(String statusCode) {
        if (errorMessages == null) {
            return false;
        }

        return errorMessages.hasGeneralErrorWithCode(statusCode);
    }

    public void setErrorMessages(ErrorMessages errorMessages) {
        this.errorMessages = errorMessages;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessages)
                .map(ErrorMessages::getAll)
                .map(msgs -> msgs.get(0))
                .map(ErrorMessage::getMessage)
                .map(Strings::emptyToNull);
    }
}
