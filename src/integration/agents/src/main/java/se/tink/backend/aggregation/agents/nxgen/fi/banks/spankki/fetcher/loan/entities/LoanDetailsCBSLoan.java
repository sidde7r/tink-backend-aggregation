package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Scanner;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanDetailsCBSLoan {
    private String currency;
    private double unwithdrawnAmount;
    private double withdrawnAmount;
    private String interestBinding;
    @JsonFormat(pattern = "yyyyMMdd")
    private Date openingDate; // ": "20161129",
    private String endDate;
    private boolean endDateEstimated;
    private String dueDate;
    private String interestPeriodFrom;
    private String interestPeriodTo;
    private double interestAmount;
    private double interestAmountFallenDue;
    private double instalmentFallenDue;
    private double overdueInterest;
    private double fees;
    private double feesFallenDue;
    private double totalAmount;
    private boolean loanInsuranceSigned;
    private String directDebitAccount1;
    private String directDebitAccount2;
    private int periodicity;
    private int respiteMonthsLeft;
    private int paymentDate;
    private boolean messageSet;
    private boolean noFee;
    private String interval;
    private boolean paymentDateMutable;
    private boolean instalmentRespiteMutable;
    private boolean directDebitAccount1Mutable;
    private boolean directDebitAccount2Mutable;
    private boolean messageMutable;
    private double unUsedAmountToday;
    private String canWithdrawFlag;
    private double interestToPay;
    private double approvedAmount;
    private double uppFee;
    private double borFee;
    private double otherFee;
    private String liftLayoutCode;
    private String nextInterestAdjustmentDate;
    private String indebtednessType;
    private double insuranceAmount;
    private double insuranceAmountFallenDue;
    private double interestCeilingPercentage;

    @JsonIgnore
    public LoanDetails toTinkLoan(LoanDetailsEntity loanDetails) {
        String loanNameFI = loanDetails.getLoanName().getFi();

        return LoanDetails.builder(getTinkLoanType(loanNameFI))
                .setLoanNumber(loanDetails.getLoanNumber())
                .setInitialBalance(new Amount(currency, -withdrawnAmount))
                .setInitialDate(openingDate)
                .setNumMonthsBound(getInterestBindingMonths())
                .build();
    }

    @JsonIgnore
    private LoanDetails.Type getTinkLoanType(String loanNameFI) {
        if (loanNameFI == null || !loanNameFI.toUpperCase().contains(SpankkiConstants.Loan.STUDENT_LOAN_NAME_FI)) {
            return LoanDetails.Type.MORTGAGE;
        }

        return LoanDetails.Type.STUDENT;
    }

    @JsonIgnore
    private int getInterestBindingMonths() {
        if (interestBinding == null) {
            return 0;
        }

        Scanner scanner = new Scanner(interestBinding);
        scanner.useDelimiter(SpankkiConstants.Loan.INTEREST_BINDING_TEXT);
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }

        return 0;
    }

    public String getCurrency() {
        return currency;
    }

    public double getUnwithdrawnAmount() {
        return unwithdrawnAmount;
    }

    public double getWithdrawnAmount() {
        return withdrawnAmount;
    }

    public String getInterestBinding() {
        return interestBinding;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean isEndDateEstimated() {
        return endDateEstimated;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getInterestPeriodFrom() {
        return interestPeriodFrom;
    }

    public String getInterestPeriodTo() {
        return interestPeriodTo;
    }

    public double getInterestAmount() {
        return interestAmount;
    }

    public double getInterestAmountFallenDue() {
        return interestAmountFallenDue;
    }

    public double getInstalmentFallenDue() {
        return instalmentFallenDue;
    }

    public double getOverdueInterest() {
        return overdueInterest;
    }

    public double getFees() {
        return fees;
    }

    public double getFeesFallenDue() {
        return feesFallenDue;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public boolean isLoanInsuranceSigned() {
        return loanInsuranceSigned;
    }

    public String getDirectDebitAccount1() {
        return directDebitAccount1;
    }

    public String getDirectDebitAccount2() {
        return directDebitAccount2;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public int getRespiteMonthsLeft() {
        return respiteMonthsLeft;
    }

    public int getPaymentDate() {
        return paymentDate;
    }

    public boolean isMessageSet() {
        return messageSet;
    }

    public boolean isNoFee() {
        return noFee;
    }

    public String getInterval() {
        return interval;
    }

    public boolean isPaymentDateMutable() {
        return paymentDateMutable;
    }

    public boolean isInstalmentRespiteMutable() {
        return instalmentRespiteMutable;
    }

    public boolean isDirectDebitAccount1Mutable() {
        return directDebitAccount1Mutable;
    }

    public boolean isDirectDebitAccount2Mutable() {
        return directDebitAccount2Mutable;
    }

    public boolean isMessageMutable() {
        return messageMutable;
    }

    public double getUnUsedAmountToday() {
        return unUsedAmountToday;
    }

    public String getCanWithdrawFlag() {
        return canWithdrawFlag;
    }

    public double getInterestToPay() {
        return interestToPay;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public double getUppFee() {
        return uppFee;
    }

    public double getBorFee() {
        return borFee;
    }

    public double getOtherFee() {
        return otherFee;
    }

    public String getLiftLayoutCode() {
        return liftLayoutCode;
    }

    public String getNextInterestAdjustmentDate() {
        return nextInterestAdjustmentDate;
    }

    public String getIndebtednessType() {
        return indebtednessType;
    }

    public double getInsuranceAmount() {
        return insuranceAmount;
    }

    public double getInsuranceAmountFallenDue() {
        return insuranceAmountFallenDue;
    }

    public double getInterestCeilingPercentage() {
        return interestCeilingPercentage;
    }
}
