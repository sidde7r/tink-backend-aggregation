package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class AccountTypeMapper {

    private static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CurrentAccount", "EMoney")
                    .put(AccountTypes.CREDIT_CARD, "CreditCard", "ChargeCard", "PrePaidCard")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .put(AccountTypes.LOAN, "Loan")
                    .put(AccountTypes.MORTGAGE, "Mortgage")
                    .build();

    private static final TypeMapper<AccountOwnershipType> ACCOUNT_OWNERSHIP_MAPPER =
            TypeMapper.<AccountOwnershipType>builder()
                    .put(AccountOwnershipType.BUSINESS, "Business")
                    .put(AccountOwnershipType.PERSONAL, "Personal")
                    .build();

    public static AccountTypes getAccountType(AccountEntity accountEntity) {
        String rawSubtype = accountEntity.getRawAccountSubType();
        return ACCOUNT_TYPE_MAPPER
                .translate(rawSubtype)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unexpected account subType:" + rawSubtype));
    }

    public static AccountOwnershipType getAccountOwnershipType(AccountEntity account) {
        String rawAccountType = account.getRawAccountType();
        return ACCOUNT_OWNERSHIP_MAPPER
                .translate(rawAccountType)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unexpected account type:" + rawAccountType));
    }
}
