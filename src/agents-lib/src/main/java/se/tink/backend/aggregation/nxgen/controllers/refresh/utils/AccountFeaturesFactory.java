package se.tink.backend.aggregation.nxgen.controllers.refresh.utils;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanInterpreter;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Portfolio;

public class AccountFeaturesFactory {

    public static <A extends Account> AccountFeatures createForAnAccount(A account,
            LoanInterpreter loanInterpreter) {
        Class<? extends Account> accountClass = account.getClass();

        if (LoanAccount.class.equals(accountClass)) {
            LoanAccount loan = (LoanAccount) account;
            return AccountFeatures.createForLoan(
                    loan.getDetails().toSystemLoan(loan, loanInterpreter));
        }

        if (InvestmentAccount.class.equals(accountClass)) {
            InvestmentAccount investment = (InvestmentAccount) account;
            return AccountFeatures.createForPortfolios(
                    investment.getPortfolios());
        }
        
        return AccountFeatures.createEmpty();
    }

    public static AccountFeatures createEmpty() {
        return AccountFeatures.createEmpty();
    }

    public static AccountFeatures createForLoan(Loan loan) {
        return AccountFeatures.createForLoan(loan);
    }

    public static AccountFeatures createForLoans(List<Loan> loans) {
        return AccountFeatures.createForLoans(loans);
    }

    public static AccountFeatures createForPortfolios(Portfolio portfolio) {
        return AccountFeatures.createForPortfolios(portfolio);
    }

    public static AccountFeatures createForPortfolios(List<Portfolio> portfolios) {
        return AccountFeatures.createForPortfolios(portfolios);
    }
}
