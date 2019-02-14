package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

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
        LoanDetails details = LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                .setLoanNumber(loanNumber)
                .setSecurity(realEstateNumber)
                .build();
        return LoanAccount.builder(getAccoutNumber())
                .setDetails(details)
                .setBalance(getBalance())
                .setName(loanTypeName)
                .setAccountNumber(getAccoutNumber())
                .build();
    }

    @JsonIgnore
    private Amount getBalance() {
        Amount balance = new Amount(currencyCode, outstandingDebt);
        if (balance.isZero()) {
            return balance;
        }

        return balance.negate();
    }

    @JsonIgnore
    private String getAccoutNumber() {
        return String.format("%s-%s", realEstateNumber, loanNumber);
    }
}
