package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;

public class SepaAccountGuesser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SepaAccountGuesser.class);
    private final ImmutableList<String> knownSavingsAccountNames = ImmutableList.of("extra-konto", "sparbrief", "vl-sparen");
    private final ImmutableList<String> accountTypeSavingsTokens = ImmutableList.of("spar");
    private final ImmutableList<String> knownInvestmentAccountNames = ImmutableList.of("direkt-depot");
    private final ImmutableList<String> accountTypeInvestmentTokens = ImmutableList.of("depot");

    public int guessSepaAccountType(String accountName) {
        if (accountName != null) {
            accountName = accountName.toLowerCase();

            for (String savingsAccountName : knownSavingsAccountNames) {
                if (accountName.equals(savingsAccountName)) {
                    return FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR;
                }
            }

            for (String investmentAccountName : knownInvestmentAccountNames) {
                if (accountName.equals(investmentAccountName)) {
                    return FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR;
                }
            }

            LOGGER.info("{} Account type is missing, product name: {}", FinTsConstants.LogTags.PRODUCTNAME_FOR_MISSING_ACCOUNT_TYPE.toString(), accountName);

            for (String savingsToken : accountTypeSavingsTokens) {
                if (accountName.contains(savingsToken)) {
                    return FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR;
                }
            }


            for (String investmentToken : accountTypeInvestmentTokens) {
                if (accountName.contains(investmentToken)) {
                    return FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR;
                }
            }
        }

        return FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR;
    }
}
