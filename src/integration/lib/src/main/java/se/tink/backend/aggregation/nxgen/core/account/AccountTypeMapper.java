package se.tink.backend.aggregation.nxgen.core.account;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.enums.AccountFlag;

public class AccountTypeMapper extends TriTypeMapper<AccountTypes, AccountFlag, AccountTypeMapper> {

    private AccountTypeMapper(AccountTypeMapper.Builder builder) {
        super(builder);
    }

    public static AccountTypeMapper.Builder builder() {
        return new AccountTypeMapper.Builder();
    }

    public static class Builder
            extends TriTypeMapper.Builder<AccountTypes, AccountFlag, AccountTypeMapper> {

        @Override
        public TriTypeMapper.Builder<AccountTypes, AccountFlag, AccountTypeMapper> buildStep() {
            return this;
        }

        @Override
        public AccountTypeMapper build() {
            return new AccountTypeMapper(this);
        }
    }
}
