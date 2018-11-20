package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;

public abstract class TransferEntity {
    TemporaryStorage temporaryStorage;
    AccountIdentifier accountIdentifier;

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public String getValueByKey(String key) {
        if (temporaryStorage == null) {
            throw new RuntimeException("Temporary storage has not been initialized");
        }

        return temporaryStorage.get(key);
    }
}
