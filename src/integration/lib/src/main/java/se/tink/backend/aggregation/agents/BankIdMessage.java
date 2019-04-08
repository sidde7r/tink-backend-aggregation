package se.tink.backend.aggregation.agents;

import com.google.common.base.Preconditions;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankIdMessage implements LocalizableEnum {
    BANKID_NO_RESPONSE(
            new LocalizableKey("No response from Mobile BankID. Have you opened the app?")),
    BANKID_ANOTHER_IN_PROGRESS(
            new LocalizableKey("You have another BankID session in progress. Please try again.")),
    BANKID_CANCELLED(new LocalizableKey("You cancelled the BankID process. Please try again.")),
    BANKID_FAILED(new LocalizableKey("The BankID authentication failed"));

    private final LocalizableKey key;

    BankIdMessage(LocalizableKey key) {
        Preconditions.checkNotNull(key);
        this.key = key;
    }

    @Override
    public LocalizableKey getKey() {
        return key;
    }
}
