package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;

public class TransferSource extends TransferEntity {
    private boolean transferable;

    private TransferSource() {

    }

    public boolean isTransferable() {
        return transferable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final TemporaryStorage temporaryStorage = new TemporaryStorage();
        private AccountIdentifier accountIdentifier;
        private boolean transferable;

        public Builder withIdentifier(AccountIdentifier accountIdentifier) {
            this.accountIdentifier = accountIdentifier;
            return this;
        }

        public Builder isTransferable(boolean transferable) {
            this.transferable = transferable;
            return this;
        }

        public Builder withKeyValue(String key, String value) {
            temporaryStorage.put(key, value);
            return this;
        }

        public TransferSource build() {
            Preconditions.checkNotNull(accountIdentifier, "Account identifier must not be null or empty.");

            TransferSource source = new TransferSource();
            source.accountIdentifier = accountIdentifier;
            source.transferable = transferable;
            source.temporaryStorage = temporaryStorage;

            return source;
        }
    }
}
