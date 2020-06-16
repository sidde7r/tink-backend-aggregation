package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.error;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.libraries.i18n.LocalizableKey;

public enum NemIdError implements AgentError {
    TIMEOUT(new LocalizableKey("Authenticating with NemID Code App timed out. Please try again.")),
    INTERRUPTED(
            new LocalizableKey(
                    "Another authentication attempt was initiated while authenticating. Please try again.")),
    REJECTED(new LocalizableKey("Authentication rejected by the user.")),
    CODEAPP_NOT_REGISTERED(
            new LocalizableKey(
                    "NemID Code App is not registered option to use. Contact bank support.")),
    LOCKED_PIN(
            new LocalizableKey(
                    "Your chosen PIN code is locked. The PIN code must be changed in your Netbank before you can log on."));

    private final LocalizableKey userMessage;

    NemIdError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public NemIdException exception() {
        return new NemIdException(this);
    }

    @Override
    public NemIdException exception(String internalMessage) {
        return new NemIdException(this, internalMessage);
    }

    @Override
    public NemIdException exception(Throwable cause) {
        return new NemIdException(this, cause);
    }

    @Override
    public NemIdException exception(LocalizableKey userMessage) {
        return new NemIdException(this, userMessage);
    }

    @Override
    public NemIdException exception(LocalizableKey userMessage, Throwable cause) {
        return new NemIdException(this, userMessage, cause);
    }
}
