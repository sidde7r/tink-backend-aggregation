package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.ParseException;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Loan;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanResponse {

    private LoanAccountEntity loanAccount;
    private LoanPaymentEntity loanPayment;

    public LoanAccountEntity getLoanAccount() {
        return loanAccount;
    }

    public void setLoanAccount(LoanAccountEntity loanAccount) {
        this.loanAccount = loanAccount;
    }

    public LoanPaymentEntity getLoanPayment() {
        return loanPayment;
    }

    public void setLoanPayment(LoanPaymentEntity loanPayment) {
        this.loanPayment = loanPayment;
    }

    public Loan toLoan(Account account, String loanResponseString) throws ParseException {
        Loan loan = new Loan();

        loan.setInterest(AgentParsingUtils.parsePercentageFormInterest(loanAccount.getInterest()));
        loan.setName(loanAccount.getName());
        loan.setLoanNumber(loanAccount.getAccountNumber());
        loan.setBalance(account.getBalance());

        String nextDayOfTermsChange = loanPayment.getDueDay();
        if (nextDayOfTermsChange != null) {
            loan.setNextDayOfTermsChange(ThreadSafeDateFormat.FORMATTER_DAILY.parse(nextDayOfTermsChange));
        }

        loan.setSerializedLoanResponse(loanResponseString);

        return loan;
    }
}
