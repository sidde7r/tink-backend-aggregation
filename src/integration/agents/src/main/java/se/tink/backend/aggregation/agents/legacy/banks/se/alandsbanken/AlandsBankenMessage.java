package se.tink.backend.aggregation.agents.banks.se.alandsbanken;

import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum AlandsBankenMessage implements LocalizableEnum {
    EXPIRED_PASSWORD("Invalid password, please contact Ålandsbanken at: 0771-415 415."),
    BLOCKED_PASSWORD("Your password is blocked, please contact Ålandsbanken at: 0771-415 415."),
    BLOCKED_USER("Authorization failed, please contact Ålandsbanken at: 0771-415 415."),
    APPROVAL_NEEDED(
            "You have unconfirmed agreements, please login to Ålandsbankens online bank using a browser."),
    PINCODE_LIST_EXCEPTION(
            "All disposable codes have been used, please contact Ålandsbanken at: 0771-415 415."),
    NEW_PIN_CODE_TABLE_FAULT(
            "All disposable codes have been used, please contact Ålandsbanken at: 0771-415 415.");

    private LocalizableKey userMessage;

    AlandsBankenMessage(String userMessage) {
        this.userMessage = new LocalizableKey(userMessage);
    }

    @Override
    public LocalizableKey getKey() {
        return userMessage;
    }
}
