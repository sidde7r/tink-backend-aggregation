package se.tink.backend.aggregation.nxgen.core.account;

import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class TransactionalAccountTypeMapper
        extends TriTypeMapper<
                TransactionalAccountType, AccountFlag, TransactionalAccountTypeMapper> {

    private TransactionalAccountTypeMapper(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
            extends TriTypeMapper.Builder<
                    TransactionalAccountType, AccountFlag, TransactionalAccountTypeMapper> {

        @Override
        public TriTypeMapper.Builder<
                        TransactionalAccountType, AccountFlag, TransactionalAccountTypeMapper>
                buildStep() {
            return this;
        }

        @Override
        public TransactionalAccountTypeMapper build() {
            return new TransactionalAccountTypeMapper(this);
        }
    }
}
