package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;

public class TransactionalAccount extends Account {
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES = ImmutableList.<AccountTypes>builder()
            .add(AccountTypes.SAVINGS)
            .add(AccountTypes.CHECKING)
            .add(AccountTypes.OTHER)
            .build();

    TransactionalAccount(String name, String accountNumber, Amount balance,
            List<AccountIdentifier> identifiers, String tinkId, String bankIdentifier, HolderName holderName,
            Map<String, String> temporaryStorage) {
        super(name, accountNumber, balance, identifiers, tinkId, bankIdentifier, holderName, temporaryStorage);
    }

    public static Builder builder(AccountTypes type, String accountNumber, Amount balance) {
        switch (type) {
        case SAVINGS:
            return SavingsAccount.builder(accountNumber, balance);
        case CHECKING:
        case OTHER:
            return CheckingAccount.builder(accountNumber, balance);
        default:
            throw new IllegalStateException(String.format("Unknown TransactionalAccount type (%s)", type));
        }
    }

    public static abstract class Builder extends Account.Builder {
        Builder(String accountNumber, Amount balance) {
            super(accountNumber, balance);
        }

        @Override
        public Builder setName(String name) {
            return (Builder) super.setName(name);
        }

        @Override
        public Builder addIdentifier(AccountIdentifier identifier) {
            return (Builder) super.addIdentifier(identifier);
        }

        @Override
        public Builder setUniqueIdentifier(String uniqueIdentifier) {
            return (Builder) super.setUniqueIdentifier(uniqueIdentifier);
        }

        @Override
        public Builder setBankIdentifier(String bankIdentifier) {
            return (Builder) super.setBankIdentifier(bankIdentifier);
        }

        @Override
        public Builder setHolderName(HolderName holderName) {
            return (Builder) super.setHolderName(holderName);
        }

        @Override
        public Builder setTemporaryStorage(Map<String, String> temporaryStorage) {
            return (Builder) super.setTemporaryStorage(temporaryStorage);
        }

        @Override
        public <T> Builder addToTemporaryStorage(String key, T value) {
            return (Builder) super.addToTemporaryStorage(key, value);
        }

        @Override
        public abstract TransactionalAccount build();
    }
}
