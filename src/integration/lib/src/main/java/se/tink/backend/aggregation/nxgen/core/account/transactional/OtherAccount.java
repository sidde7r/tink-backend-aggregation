package se.tink.backend.aggregation.nxgen.core.account.transactional;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AccountIdentifierStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AccountNumberStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.OtherBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.UniqueIdentifierStep;
import se.tink.libraries.amount.Amount;

public class OtherAccount extends TransactionalAccount {
    private static final Logger LOG = LoggerFactory.getLogger(OtherAccount.class);

    @Deprecated
    private OtherAccount(Builder<OtherAccount, DefaultOtherAccountBuilder> builder) {
        super(builder);
        LOG.info("Unknown_account_type - deprecated " + this.getAccountNumber());
    }

    private OtherAccount(OtherAccountBuilder builder) {
        super(builder);
        LOG.info("Unknown_account_type " + this.getAccountNumber());
    }


    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultOtherAccountBuilder(uniqueIdentifier);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier)
                .setBalance(balance);
    }

    public static UniqueIdentifierStep<OtherBuildStep> builder() {
        return new OtherAccountBuilder();
    }

    private static class OtherAccountBuilder
            extends StepBuilder<OtherAccount, OtherBuildStep>
            implements UniqueIdentifierStep<OtherBuildStep>,
            AccountNumberStep<OtherBuildStep>,
            BalanceStep<OtherBuildStep>,
            AccountIdentifierStep<OtherBuildStep>,
            OtherBuildStep {

        private Double interestRate;

        @Override
        public AccountNumberStep<OtherBuildStep> setUniqueIdentifier(
                @Nonnull String uniqueIdentifier) {
            applyUniqueIdentifier(uniqueIdentifier);
            return this;
        }

        @Override
        public BalanceStep<OtherBuildStep> setAccountNumber(@Nonnull String accountNumber) {
            applyAccountNumber(accountNumber);
            return this;
        }

        @Override
        public AccountIdentifierStep<OtherBuildStep> setBalance(@Nonnull Amount balance) {
            applyBalance(balance);
            return this;
        }

        @Override
        public OtherAccount build() {
            return new OtherAccount(this);
        }

        Double getInterestRate() {
            return interestRate;
        }

        @Override
        protected OtherBuildStep buildStep() {
            return this;
        }
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.OTHER;
    }

    /** @deprecated Use OtherAccountBuilder instead */
    @Deprecated
    public abstract static class Builder<A extends OtherAccount, T extends OtherAccount.Builder<A, T>>
            extends TransactionalAccount.Builder<OtherAccount, Builder<A, T>> {

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }
    }

    /** @deprecated Use OtherAccountBuilder instead */
    @Deprecated
    public static class DefaultOtherAccountBuilder
            extends OtherAccount.Builder<OtherAccount, DefaultOtherAccountBuilder> {

        public DefaultOtherAccountBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public OtherAccount build() {
            return new OtherAccount(self());
        }
    }
}
