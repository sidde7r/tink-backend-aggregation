package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.agents.rpc.AccountTypes;

public class FinTsAccountTypeConverter {
    public static AccountTypes getAccountTypeFor(int finTsAccountType) {

        if(between(finTsAccountType, FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR, FinTsConstants.AccountType.TIME_DEPOSIT_ACCOUNT_CURSOR))
            return AccountTypes.SAVINGS;
        if(between(finTsAccountType, FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR, FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR))
            return AccountTypes.CHECKING;
        if(between(finTsAccountType, FinTsConstants.AccountType.CREDIT_CARD_CURSOR, FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR))
            return AccountTypes.CREDIT_CARD;

        return AccountTypes.OTHER;
    }

    private static boolean between(int i, int minInclusive, int max) {
        return (i >= minInclusive && i < max);
    }
}
