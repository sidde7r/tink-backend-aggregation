package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.AccountNameStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.AccountNumberStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdentifierStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.UniqueIdStep;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.strings.StringUtils;

@EqualsAndHashCode
public final class IdModule {

    private final String uniqueId;
    private final String accountNumber;
    private final String accountName;
    private final String productName;
    private final Set<AccountIdentifier> identifiers;

    private IdModule(Builder builder) {
        this.uniqueId = builder.uniqueIdentifier;
        this.accountNumber = builder.accountNumber;
        this.accountName = builder.accountName;
        this.identifiers = builder.identifiers;
        this.productName = builder.productName;
    }

    public static UniqueIdStep<IdBuildStep> builder() {
        return new Builder();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public Set<AccountIdentifier> getIdentifiers() {
        return ImmutableSet.copyOf(identifiers);
    }

    public String getProductName() {
        return productName;
    }

    private static class Builder
            implements UniqueIdStep<IdBuildStep>,
                    AccountNumberStep<IdBuildStep>,
                    AccountNameStep<IdBuildStep>,
                    IdentifierStep<IdBuildStep>,
                    IdBuildStep {
        private final Set<AccountIdentifier> identifiers = new HashSet<>();
        private String uniqueIdentifier;
        private String accountNumber;
        private String accountName;
        private String productName;

        @Override
        public AccountNameStep<IdBuildStep> withAccountNumber(@Nonnull String accountNumber) {
            Preconditions.checkNotNull(accountNumber, "AccountNumber must not be null.");

            this.accountNumber = accountNumber;
            return this;
        }

        @Override
        public IdBuildStep addIdentifier(@Nonnull AccountIdentifier identifier) {
            Preconditions.checkNotNull(identifier, "AccountIdentifier must not be null.");

            if (identifiers.add(identifier)) {
                return this;
            }

            throw new IllegalArgumentException(
                    String.format(
                            "Identifier %s is already present in the set.", identifier.getType()));
        }

        @Override
        public IdBuildStep addIdentifiers(@Nonnull Collection<AccountIdentifier> identifiers) {
            Preconditions.checkArgument(
                    !identifiers.isEmpty(), "Identifiers list must not be empty");
            identifiers.forEach(this::addIdentifier);
            return this;
        }

        @Override
        public IdentifierStep<IdBuildStep> withAccountName(@Nonnull String name) {
            Preconditions.checkNotNull(name, "AccountName must not be null.");

            this.accountName = name;
            return this;
        }

        @Override
        public AccountNumberStep<IdBuildStep> withUniqueIdentifier(@Nonnull String identifier) {
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(identifier),
                    "UniqueIdentifier must not be null or empty.");

            final String trimmedIdentifier = StringUtils.removeNonAlphaNumeric(identifier);

            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(trimmedIdentifier),
                    "UniqueIdentifier was empty after sanitation.");

            this.uniqueIdentifier = trimmedIdentifier;
            return this;
        }

        @Override
        public IdModule build() {
            return new IdModule(this);
        }

        @Override
        public IdBuildStep setProductName(@Nullable String productName) {
            this.productName = productName;
            return this;
        }
    }
}
