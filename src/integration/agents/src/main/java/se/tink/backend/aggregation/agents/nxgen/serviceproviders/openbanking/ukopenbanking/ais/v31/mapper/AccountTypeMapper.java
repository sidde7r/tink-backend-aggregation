package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public interface AccountTypeMapper {
    TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CurrentAccount", "EMoney")
                    .put(AccountTypes.CREDIT_CARD, "CreditCard", "ChargeCard", "PrePaidCard")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .put(AccountTypes.LOAN, "Loan")
                    .put(AccountTypes.MORTGAGE, "Mortgage")
                    .build();

    TypeMapper<AccountOwnershipType> ACCOUNT_OWNERSHIP_MAPPER =
            TypeMapper.<AccountOwnershipType>builder()
                    .put(AccountOwnershipType.BUSINESS, "Business")
                    .put(AccountOwnershipType.PERSONAL, "Personal")
                    .build();

    default AccountTypes getAccountType(AccountEntity accountEntity) {
        String rawSubtype = accountEntity.getRawAccountSubType();
        return ACCOUNT_TYPE_MAPPER
                .translate(rawSubtype)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unexpected account subType:" + rawSubtype));
    }

    default AccountOwnershipType getAccountOwnershipType(AccountEntity account) {
        String rawAccountType = account.getRawAccountType();
        return ACCOUNT_OWNERSHIP_MAPPER
                .translate(rawAccountType)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unexpected account type:" + rawAccountType));
    }

    boolean supportsAccountOwnershipType(AccountEntity accountEntity);
}
