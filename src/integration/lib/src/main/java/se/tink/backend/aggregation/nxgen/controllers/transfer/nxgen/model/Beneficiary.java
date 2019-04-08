package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;

public class Beneficiary extends TransferEntity {
    private String name;

    private Beneficiary() {}

    public String getName() {
        return name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final TemporaryStorage temporaryStorage = new TemporaryStorage();
        private String name;
        private AccountIdentifier accountIdentifier;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withAccountIdentifier(AccountIdentifier accountIdentifier) {
            this.accountIdentifier = accountIdentifier;
            return this;
        }

        public Builder withKeyValue(String key, String value) {
            temporaryStorage.put(key, value);
            return this;
        }

        public Beneficiary build() {
            Preconditions.checkState(
                    !Strings.isNullOrEmpty(name), "Name must not be null or empty.");
            Preconditions.checkNotNull(
                    accountIdentifier, "Account identifier must not be null or empty.");

            Beneficiary command = new Beneficiary();
            command.name = name;
            command.accountIdentifier = accountIdentifier;
            command.temporaryStorage = temporaryStorage;

            return command;
        }
    }
}
