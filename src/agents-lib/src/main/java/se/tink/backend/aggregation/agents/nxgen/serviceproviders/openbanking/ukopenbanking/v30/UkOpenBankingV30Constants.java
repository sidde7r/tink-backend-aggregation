package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class UkOpenBankingV30Constants extends UkOpenBankingConstants {

    public static class Links {
        public static final String NEXT = "Next";
    }

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER = AccountTypeMapper.builder()
            .put(AccountTypes.CHECKING, "CurrentAccount")
            .put(AccountTypes.CREDIT_CARD, "CreditCard")
            .put(AccountTypes.SAVINGS, "Savings")
            .put(AccountTypes.LOAN, "Loan")
            .put(AccountTypes.MORTGAGE, "Mortgage")
            .add("ChargeCard", "EMoney", "PrePaidCard")
            .build();

}
