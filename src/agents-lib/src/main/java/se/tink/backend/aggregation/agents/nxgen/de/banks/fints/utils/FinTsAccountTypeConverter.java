package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class FinTsAccountTypeConverter {
    public static AccountTypes getAccountTypeFor(int finTsAccountType) {

        if(between(finTsAccountType, FinTsConstants.AccountType.SAVINGS_ACCOUNT_MIN, FinTsConstants.AccountType.SAVINGS_ACCOUNT_MAX))
            return AccountTypes.SAVINGS;
        if(between(finTsAccountType, FinTsConstants.AccountType.CURRENT_ACCOUNT_MIN, FinTsConstants.AccountType.CURRENT_ACCOUNT_MAX))
            return AccountTypes.CHECKING;
        if(between(finTsAccountType, FinTsConstants.AccountType.CREDIT_CARD_MIN, FinTsConstants.AccountType.CREDIT_CARD_MAX))
            return AccountTypes.CREDIT_CARD;

        return AccountTypes.OTHER;
    }

    private static boolean between(int i, int min, int max) {
        return (i >= min && i <= max);
    }
}
