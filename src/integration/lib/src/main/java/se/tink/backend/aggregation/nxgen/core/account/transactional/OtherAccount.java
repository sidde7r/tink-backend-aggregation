package se.tink.backend.aggregation.nxgen.core.account.transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;

/**
 * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
 *     <p>This will be removed as part of the improved step builder + agent builder refactoring
 *     project
 */
@Deprecated
public class OtherAccount extends TransactionalAccount {
    private static final Logger LOG = LoggerFactory.getLogger(OtherAccount.class);

    @Deprecated
    private OtherAccount(Builder<OtherAccount, DefaultOtherAccountBuilder> builder) {
        super(builder);
        LOG.info("Unknown_account_type - deprecated " + this.getName());
    }

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
    @Deprecated
    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultOtherAccountBuilder(uniqueIdentifier);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.OTHER;
    }

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
    @Deprecated
    public abstract static class Builder<
                    A extends OtherAccount, T extends OtherAccount.Builder<A, T>>
            extends TransactionalAccount.Builder<OtherAccount, Builder<A, T>> {

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }
    }

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
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
