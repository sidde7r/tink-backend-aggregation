package se.tink.backend.common.loans;

import java.io.Serializable;
import se.tink.backend.core.Loan;

@Deprecated()
// This is backwards compability since it is hard to find the market sometimes
public class SwedishLoanNameInterpreter implements Serializable {

    private Integer numMonthsBound;
    private Loan.Type loanType;

    public SwedishLoanNameInterpreter(String loanName) {
        LoanNameInterpreter li = LoanNameInterpreter.getInstance("SE", loanName);

        loanType = li.getGuessedLoanType();
        numMonthsBound = li.getGuessedNumMonthsBound();
    }

    public Loan.Type getGuessedLoanType() {
        return loanType;
    }

    public Integer getGuessedNumMonthsBound() {
        return numMonthsBound;
    }
}
