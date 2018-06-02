package se.tink.backend.product.execution.unit.agents.exceptions.errors;

import se.tink.backend.product.execution.unit.agents.exceptions.LoginException;
import se.tink.libraries.i18n.LocalizableKey;

public enum LoginError implements AgentError {
    NOT_CUSTOMER(new LocalizableKey("You don't have any commitments in the selected bank.")),
    NOT_SUPPORTED(new LocalizableKey("The authentication needed to login to your bank is not supported at the moment. If possible please try another option.")),
    INCORRECT_CREDENTIALS(new LocalizableKey("Incorrect login credentials. Please try again.")),
    INCORRECT_CHALLENGE_RESPONSE(new LocalizableKey("Incorrect challenge response. Please try again.")),
    CREDENTIALS_VERIFICATION_ERROR(new LocalizableKey("Your credentials could not be verified. Please try again.")),
    WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE(new LocalizableKey("Please ensure that you have entered the correct mobile number and date of birth. Also make sure that you have activated bankID on mobile in your internet bank.")), // NO
    ERROR_WITH_MOBILE_OPERATOR(new LocalizableKey("There is an error with your mobile operator. Please try again later.")); // NO


    private LocalizableKey userMessage;

    LoginError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LoginException exception() {
        return new LoginException(this);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public LoginException exception(LocalizableKey userMessage) {
        return new LoginException(this, userMessage);
    }
}
