package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.AccountNameStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.AccountNumberStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdentifierStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.ProductNameStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.UniqueIdStep;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.strings.StringUtils;

public class IdModule {

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
        return identifiers;
    }

    public String getProductName() {
        return productName;
    }

    private static class Builder
            implements UniqueIdStep<IdBuildStep>,
                    AccountNumberStep<IdBuildStep>,
                    AccountNameStep<IdBuildStep>,
                    ProductNameStep<IdBuildStep>,
                    IdentifierStep<IdBuildStep>,
                    IdBuildStep {
        private final Set<AccountIdentifier> identifiers = new HashSet<>();
        private String uniqueIdentifier;
        private String accountNumber;
        private String accountName;
        private String productName;

        @Override
        public AccountNameStep<IdBuildStep> setAccountNumber(@Nonnull String accountNumber) {
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
        public ProductNameStep<IdBuildStep> setAccountName(String name) {
            this.accountName = name;
            return this;
        }

        @Override
        public AccountNumberStep<IdBuildStep> setUniqueIdentifier(@Nonnull String identifier) {
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(identifier),
                    "UniqueIdentifier must no be null or empty.");

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
        public IdentifierStep<IdBuildStep> setProductName(String productName) {
            this.productName = productName;
            return this;
        }
    }
}
