package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CollateralCreditDetailsResponse extends OpBankResponseEntity {
    @JsonFormat(pattern = "y-M-d")
    private Date firstWithdrawalDate;

    private double interestRate;
    private String interestSupportCode;
    private double interestSupportRate;
    private int interestPaymentPeriod;

    @JsonFormat(pattern = "y-M-d")
    private Date interestVerificationDate;

    private int creditType;

    @JsonFormat(pattern = "y-M-d")
    private Date instalmentDueDate;

    private double instalmentAmount;
    private String instalmentMethod;
    private int instalmentPeriod;
    private double balance;
    private String debitAccount;
    private String interestCertificateText;
    private String repaymentSecurityCode;
    private double capRate;
    private double paidInterestPreviousYear;
    private double paidInterestCurrentYear;
    private double paidInstalmentPreviousYear;
    private double paidInstalmentCurrentYear;
    private String referenceRate;
    private double marginalPercentage;
    private String instrumentId;

    @JsonIgnore
    public LoanAccount toLoanAccount(OpBankCreditEntity creditEntity) {
        return LoanAccount.builder(creditEntity.getAgreementNumberIban(), Amount.inEUR(balance))
                .setAccountNumber(creditEntity.getAgreementNumberIban())
                .setInterestRate(interestRate)
                .setBankIdentifier(creditEntity.getAgreementNumberIban())
                .setName(creditEntity.getLoanName())
                .setDetails(
                        LoanDetails.builder(
                                        OpBankConstants.LoanType.findLoanType(
                                                        creditEntity.getUsage())
                                                .getTinkType())
                                .setLoanNumber(creditEntity.getAgreementNumberIban())
                                .setInitialBalance(
                                        Amount.inEUR(creditEntity.getCalculatedWithdrawnAmount()))
                                .setInitialDate(firstWithdrawalDate)
                                .build())
                .build();
    }

    public Date getFirstWithdrawalDate() {
        return firstWithdrawalDate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public String getInterestSupportCode() {
        return interestSupportCode;
    }

    public double getInterestSupportRate() {
        return interestSupportRate;
    }

    public int getInterestPaymentPeriod() {
        return interestPaymentPeriod;
    }

    public Date getInterestVerificationDate() {
        return interestVerificationDate;
    }

    public int getCreditType() {
        return creditType;
    }

    public Date getInstalmentDueDate() {
        return instalmentDueDate;
    }

    public double getInstalmentAmount() {
        return instalmentAmount;
    }

    public String getInstalmentMethod() {
        return instalmentMethod;
    }

    public int getInstalmentPeriod() {
        return instalmentPeriod;
    }

    public double getBalance() {
        return balance;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public String getInterestCertificateText() {
        return interestCertificateText;
    }

    public String getRepaymentSecurityCode() {
        return repaymentSecurityCode;
    }

    public double getCapRate() {
        return capRate;
    }

    public double getPaidInterestPreviousYear() {
        return paidInterestPreviousYear;
    }

    public double getPaidInterestCurrentYear() {
        return paidInterestCurrentYear;
    }

    public double getPaidInstalmentPreviousYear() {
        return paidInstalmentPreviousYear;
    }

    public double getPaidInstalmentCurrentYear() {
        return paidInstalmentCurrentYear;
    }

    public String getReferenceRate() {
        return referenceRate;
    }

    public double getMarginalPercentage() {
        return marginalPercentage;
    }

    public String getInstrumentId() {
        return instrumentId;
    }
}
