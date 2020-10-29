package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RepaymentSchedule {

    @JsonProperty("initial_payment_date")
    private String initialPaymentDate;

    @JsonProperty("period_between_instalments")
    private int periodBetweenInstalments;

    @JsonProperty("debit_account_number")
    private String debitAccountNumber;

    @JsonProperty("loan_account_number")
    private String loanAccountNumber;

    @JsonProperty("instalment_free_months")
    private List<Object> instalmentFreeMonths;

    public String getInitialPaymentDate() {
        return initialPaymentDate;
    }

    public int getPeriodBetweenInstalments() {
        return periodBetweenInstalments;
    }

    public String getDebitAccountNumber() {
        return debitAccountNumber;
    }

    public String getLoanAccountNumber() {
        return loanAccountNumber;
    }

    public List<Object> getInstalmentFreeMonths() {
        return instalmentFreeMonths;
    }
}
