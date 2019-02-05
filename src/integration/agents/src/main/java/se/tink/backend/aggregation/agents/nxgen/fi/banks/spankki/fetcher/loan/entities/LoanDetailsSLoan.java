package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class LoanDetailsSLoan {
    private String beginDate;
    private double originalAmount;
    private long timeLeft;
    private int numberOfPaymentsLeft;
    private String previousInterestUpdateDate;
    private long interestUpdatePeriod;
    private String finalDueDate;
    private double margin;
    private String balanceDate;
    private boolean insurance;
    private String nextInterestUpdate;
    private double delinquenciesTotal;

    @JsonIgnore
    public LoanDetails toTinkLoan(LoanDetailsEntity loanDetails) {

        return LoanDetails.builder(LoanDetails.Type.BLANCO)
                .setLoanNumber(loanDetails.getLoanNumber())
                .setInitialBalance(Amount.inEUR(-originalAmount))
                .setInitialDate(getBeginDateParsed())
                .build();
    }

    @JsonIgnore
    public Date getBeginDateParsed() {
        try {
            return ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITH_TIMEZONE.parse(beginDate);
        } catch (ParseException pe) {
            throw new RuntimeException("Failed to parse begin date", pe);
        }
    }
}
