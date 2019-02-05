package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;

public class TransferDestination extends TransferEntity {
    private TransferDestination() {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final TemporaryStorage temporaryStorage = new TemporaryStorage();
        private AccountIdentifier accountIdentifier;

        public Builder putInTemporaryStorage(String key, String value) {
            temporaryStorage.put(key, value);
            return this;
        }

        public Builder withKeyValue(String key, String value) {
            temporaryStorage.put(key, value);
            return this;
        }

        public Builder withIdentifier(AccountIdentifier accountIdentifier) {
            this.accountIdentifier = accountIdentifier;
            return this;
        }

        public TransferDestination build() {
            Preconditions.checkNotNull(accountIdentifier, "Account identifier must not be null or empty.");

            TransferDestination source = new TransferDestination();
            source.temporaryStorage = temporaryStorage;
            source.accountIdentifier = accountIdentifier;

            return source;
        }
    }
}
