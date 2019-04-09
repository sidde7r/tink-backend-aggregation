package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Loan;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement()
public class LoanDetailsResponse {

    @JsonProperty("getLoanDetailsOut")
    private LoanDetailsEntity loanDetails;

    public LoanDetailsEntity getLoanDetails() {
        return loanDetails;
    }

    public void setLoanDetails(LoanDetailsEntity loanDetails) {
        this.loanDetails = loanDetails;
    }

    public Loan toLoan(Account account, Loan.Type loanType, String loanResponseContent)
            throws ParseException {
        Loan loan = new Loan();
        LoanData loanData = loanDetails.getLoanData();
        LoanPaymentDetails followingPayment = loanDetails.getFollowingPayment();

        loan.setName(account.getName());
        loan.setLoanNumber(loanData.getLocalNumber());
        loan.setInterest(loanData.getInterest());
        loan.setSerializedLoanResponse(loanResponseContent);
        loan.setBalance(loanData.getBalance());
        loan.setInitialBalance(loanData.getGranted());
        loan.setNextDayOfTermsChange(loanData.getInterestTermEnds());
        loan.setMonthlyAmortization(followingPayment.getAmortization());

        if (loan.getBalance() != null && loan.getInitialBalance() != null) {
            loan.setAmortized(loan.getBalance() - loan.getInitialBalance());
        }

        loan.setType(loanType);

        return loan;
    }
}
