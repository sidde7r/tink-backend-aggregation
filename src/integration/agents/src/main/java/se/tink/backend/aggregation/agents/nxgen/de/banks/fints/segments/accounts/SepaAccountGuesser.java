package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;

public class SepaAccountGuesser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SepaAccountGuesser.class);

    public int guessSepaAccountType(String accountName) {
        if (accountName != null) {
            accountName = accountName.toLowerCase();

            for (String savingsAccountName :
                    FinTsConstants.SepaAccountIdentifiers.KNOWN_SAVINGS_ACCOUNT_NAMES) {
                if (accountName.equalsIgnoreCase(savingsAccountName)) {
                    return FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR;
                }
            }

            for (String investmentAccountName :
                    FinTsConstants.SepaAccountIdentifiers.KNOWN_INVESTMENT_ACCOUNT_NAMES) {
                if (accountName.equalsIgnoreCase(investmentAccountName)) {
                    return FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR;
                }
            }

            for (String creditAccountName :
                    FinTsConstants.SepaAccountIdentifiers.KNOWN_CREDIT_ACCOUNT_NAMES) {
                if (accountName.equalsIgnoreCase(creditAccountName)) {
                    return FinTsConstants.AccountType.CREDIT_CARD_CURSOR;
                }
            }

            for (String creditAccountName :
                    FinTsConstants.SepaAccountIdentifiers.KNOWN_CHECKING_ACCOUNT_NAMES) {
                if (accountName.equalsIgnoreCase(creditAccountName)) {
                    return FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR;
                }
            }

            for (String savingsToken :
                    FinTsConstants.SepaAccountIdentifiers.ACCOUNT_TYPE_SAVINGS_TOKENS) {
                if (StringUtils.containsIgnoreCase(accountName, savingsToken)) {
                    return FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR;
                }
            }

            for (String investmentToken :
                    FinTsConstants.SepaAccountIdentifiers.ACCOUNT_TYPE_INVESTMENT_TOKENS) {
                if (StringUtils.containsIgnoreCase(accountName, investmentToken)) {
                    return FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR;
                }
            }

            for (String creditToken :
                    FinTsConstants.SepaAccountIdentifiers.ACCOUNT_TYPE_CREDIT_TOKENS) {
                if (StringUtils.containsIgnoreCase(accountName, creditToken)) {
                    return FinTsConstants.AccountType.CREDIT_CARD_CURSOR;
                }
            }
        }

        LOGGER.info(
                "{} Account type is missing, product name: {}",
                FinTsConstants.LogTags.PRODUCTNAME_FOR_MISSING_ACCOUNT_TYPE.toString(),
                accountName);

        return FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR;
    }
}
