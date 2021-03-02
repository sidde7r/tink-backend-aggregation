package se.tink.backend.aggregation.agents.exceptions.nemid;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum NemIdError implements AgentError {
    TIMEOUT(new LocalizableKey("Authenticating with NemID Code App timed out. Please try again.")),
    INTERRUPTED(
            new LocalizableKey(
                    "Another authentication attempt was initiated while authenticating. Please try again.")),
    REJECTED(new LocalizableKey("Authentication rejected by the user.")),
    CODE_TOKEN_NOT_SUPPORTED(
            new LocalizableKey(
                    "NemID code token authentication is not supported. Please log in to your bank using NemID mobile app or code card and try again.")),
    SECOND_FACTOR_NOT_REGISTERED(
            new LocalizableKey(
                    "Second factor is not registered option to use. Contact bank support.")),
    INVALID_CODE_CARD_CODE(new LocalizableKey("The code card code provided by user is incorrect.")),
    USE_NEW_CODE_CARD(
            new LocalizableKey(
                    "All code card codes have been used. Please log in to your bank using new code card or NemID mobile app and try again.")),
    INVALID_CODE_TOKEN_CODE(
            new LocalizableKey("The code token code provided by user is incorrect.")),
    NEMID_LOCKED(new LocalizableKey("Your NemID is temporarily locked.")),
    NEMID_BLOCKED(new LocalizableKey("Your NemID has been blocked. Please contact NemID support.")),
    NEMID_PASSWORD_BLOCKED(
            new LocalizableKey("NemID password blocked. Please contact NemID support.")),
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
