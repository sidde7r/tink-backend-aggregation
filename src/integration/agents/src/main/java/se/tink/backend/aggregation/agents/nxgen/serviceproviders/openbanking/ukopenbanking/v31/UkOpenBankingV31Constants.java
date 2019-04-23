package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class UkOpenBankingV31Constants extends UkOpenBankingConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CurrentAccount")
                    .put(AccountTypes.CREDIT_CARD, "CreditCard")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .put(AccountTypes.LOAN, "Loan")
                    .put(AccountTypes.MORTGAGE, "Mortgage")
                    .ignoreKeys("ChargeCard", "EMoney", "PrePaidCard")
                    .build();

    public static final class ApiServices extends UkOpenBankingConstants.ApiServices {
        public static final String CONSENT_REQUEST = "/account-access-consents";
        public static final String AISP_PREFIX = "/aisp";
        public static final String PISP_PREFIX = "/pisp";
    }

    public static class Links {
        public static final String NEXT = "Next";
    }
}
