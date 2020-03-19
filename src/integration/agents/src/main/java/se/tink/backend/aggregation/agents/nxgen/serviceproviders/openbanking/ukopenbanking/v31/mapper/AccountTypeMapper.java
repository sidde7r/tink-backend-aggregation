package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class AccountTypeMapper {

    private static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CurrentAccount")
                    .put(AccountTypes.CREDIT_CARD, "CreditCard")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .put(AccountTypes.LOAN, "Loan")
                    .put(AccountTypes.MORTGAGE, "Mortgage")
                    .ignoreKeys("ChargeCard", "EMoney", "PrePaidCard")
                    .build();

    public AccountTypes getAccountType(AccountEntity accountEntity) {
        String rawSubtype = accountEntity.getRawAccountSubType();
        return ACCOUNT_TYPE_MAPPER
                .translate(rawSubtype)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unexpected account type:" + rawSubtype));
    }
}
