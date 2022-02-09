package se.tink.backend.aggregation.agents.exceptions.mitid;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum MitIdError implements AgentError {
    MIT_ID_METHOD_NOT_AVAILABLE(
            new LocalizableKey("MitID method is not available for your account.")),
    ONLY_CODE_APP_METHOD_SUPPORTED(
            new LocalizableKey(
                    "Currently, the only MitID authentication method support is MitID app. Please enable it and try again.")),

    INVALID_USER_ID_FORMAT(new LocalizableKey("Invalid user ID format.")),
    USER_ID_DOES_NOT_EXIST(new LocalizableKey("Provided user ID does not exist.")),

    CODE_APP_TOO_MANY_REQUESTS(
            new LocalizableKey(
                    "Your MitID app has received several requests at the same time. All the requests have been cancelled for security reasons.")),
    CODE_APP_TEMPORARILY_LOCKED(
            new LocalizableKey("Your MitID app is temporarily blocked, please try again later.")),
    CODE_APP_TIMEOUT(new LocalizableKey("MitID code app request timed out.")),
    CODE_APP_REJECTED(new LocalizableKey("MitID code app request was rejected.")),
    CODE_APP_TECHNICAL_ERROR(
            new LocalizableKey(
                    "MitID code app encountered unknown technical error. Please try again.")),

    INVALID_CPR_FORMAT(new LocalizableKey("Invalid CPR format.")),
    INVALID_CPR(new LocalizableKey("Invalid CPR number.")),

    SESSION_TIMEOUT(new LocalizableKey("MitID authentication took too long - session timed out.")),
    UNSPECIFIED_ERROR(
            new LocalizableKey("MitID authentication has failed with an unspecified error.")),
    UNKNOWN_ERROR_NOTIFICATION(
            new LocalizableKey("MitID authentication has failed with an unknown error message.")),
    CANNOT_FIND_ERROR_NOTIFICATION(
            new LocalizableKey("MitID authentication has failed with an unknown error message."));

    private final LocalizableKey userMessage;

    MitIdError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public MitIdException exception() {
        return new MitIdException(this);
    }

    @Override
    public MitIdException exception(String internalMessage) {
        String internalMessageExtended =
                String.format("MitID error: %s. Internal message: ( %s )", this, internalMessage);
        return new MitIdException(this, internalMessageExtended);
    }

    @Override
    public MitIdException exception(Throwable cause) {
        return new MitIdException(this, cause);
    }

    @Override
    public MitIdException exception(LocalizableKey userMessage) {
        return new MitIdException(this, userMessage);
    }

    @Override
    public MitIdException exception(LocalizableKey userMessage, Throwable cause) {
        return new MitIdException(this, userMessage, cause);
    }
}
