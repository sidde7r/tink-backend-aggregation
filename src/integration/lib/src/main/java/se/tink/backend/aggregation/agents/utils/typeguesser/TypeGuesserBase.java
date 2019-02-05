package se.tink.backend.aggregation.agents.utils.typeguesser;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;

abstract class TypeGuesserBase {
    protected final ImmutableList<String> accountTypeSavingsTokens;
    protected final ImmutableList<String> accountTypeInvestmentTokens;

    TypeGuesserBase(ImmutableList<String> accountTypeSavingsTokens, ImmutableList<String> accountTypeInvestmentTokens) {
        this.accountTypeInvestmentTokens = accountTypeInvestmentTokens;
        this.accountTypeSavingsTokens = accountTypeSavingsTokens;
    }

    /**
     * Guess type of account based name for a specific language.
     * Expects Language based tokens to compare.
     * @param accountName
     * @return Account type, best guess
     */
    AccountTypes guessAccountType(String accountName) {
        if (accountName != null) {
            accountName = accountName.toLowerCase();

            for (String savingsToken : accountTypeSavingsTokens) {
                if (accountName.contains(savingsToken)) {
                    return AccountTypes.SAVINGS;
                }
            }

            for (String investmentToken : accountTypeInvestmentTokens) {
                if (accountName.contains(investmentToken)) {
                    return AccountTypes.INVESTMENT;
                }
            }
        }

        return AccountTypes.CHECKING;
    }
}
