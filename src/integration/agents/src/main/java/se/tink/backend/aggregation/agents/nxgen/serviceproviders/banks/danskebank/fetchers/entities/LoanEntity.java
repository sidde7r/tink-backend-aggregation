package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LoanEntity {
    private String realEstateNumber;
    private String loanNumber;
    private int newAgreementPaymentAmount;
    private int newAgreementOutstandingDebt;
    private String agreementMark;
    private String interestMark;
    private String refinanceDate;
    private String loanExpirationDate;
    private String loanStatus;
    private String newAgreementTypeName;
    private String loanType;
    private String detailMark;
    private String currencyCode;
    private int outstandingDebt;
    private double paymentAmount;
    private String paymentFrequency;
    private String loanTypeName;
    private String propertyAddress;

    public String getRealEstateNumber() {
        return realEstateNumber;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    @JsonIgnore
    public LoanAccount toTinkLoan(LoanDetailsResponse loanDetailsResponse) {
        LoanDetails details =
                LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                        .setLoanNumber(loanNumber)
                        .setSecurity(realEstateNumber)
                        .build();
        return LoanAccount.builder(getAccountNumber())
                .setDetails(details)
                .setExactBalance(getBalance())
                .setName(loanTypeName)
                .setAccountNumber(getAccountNumber())
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .setInterestRate(parseInterestRate(loanDetailsResponse))
                .sourceInfo(
                        AccountSourceInfo.builder()
                                .bankProductName(loanTypeName)
                                .bankProductCode(loanType)
                                .build())
                .build();
    }

    @JsonIgnore
    Double parseInterestRate(LoanDetailsResponse loanDetailsResponse) {
        try {
            BigDecimal interest = new BigDecimal(loanDetailsResponse.getLoanDetail().getInterest());
            BigDecimal frequency =
                    new BigDecimal(loanDetailsResponse.getLoanDetail().getPaymentFrequency());
            BigDecimal cashDebt = new BigDecimal(loanDetailsResponse.getLoanDetail().getCashDebt());
            return interest.multiply(frequency)
                    .divide(cashDebt, 6, RoundingMode.HALF_UP)
                    .doubleValue();
        } catch (NumberFormatException | NullPointerException | ArithmeticException e) {
            return null;
        }
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        ExactCurrencyAmount balance =
                ExactCurrencyAmount.of(new BigDecimal(outstandingDebt), currencyCode);
        if (balance.getExactValue().compareTo(BigDecimal.ZERO) == 0) {
            return balance;
        }

        return balance.negate();
    }

    @JsonIgnore
    private String getAccountNumber() {
        return String.format("%s-%s", realEstateNumber, loanNumber);
    }
}
