package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanEntity {
    private String name;
    private String loanNumber;
    private String loanNumberGuid;
    private String maskertKontonummer;
    private double balance;
    private double availableBalance;
    private String kontotype;
    private String hovedKontotype;
    private int type;
    private AccessEntity access;
    private boolean isOwner;
    private boolean isGuarantor;
    private boolean isHomeLoan;
    private boolean isCarLoan;
    private boolean isLeasedCarLoan;
    private boolean isBoatLoan;
    private boolean isConsumerLoan;
    private boolean isFixedInterestLoan;
    private boolean isTopplaan;
    private boolean isCurrencyLoan;
    private boolean isFlexiLoan;
    private boolean isByggelaan;
    private boolean isPaymentLoan;
    private boolean isLeasing;

    public String getLoanNumberGuid() {
        return loanNumberGuid;
    }

    public int getType() {
        return type;
    }

    public boolean isCurrencyLoan() {
        return isCurrencyLoan;
    }

    public LoanAccount toTinkLoan(LoanDetailsResponse loanDetailsResponse) {
        LoanDetails loanDetails = LoanDetails.builder(getLoanType())
                .setLoanNumber(loanNumber)
                .setInitialBalance(Amount.inNOK(loanDetailsResponse.getInitialBalanace()))
                .build();
        return LoanAccount.builder(loanNumber, Amount.inNOK(balance))
                .setAccountNumber(loanNumber)
                .setName(name)
                .setInterestRate(loanDetailsResponse.getNominalInterestRate())
                .setBankIdentifier(loanNumber)
                .setDetails(loanDetails)
                .build();
    }

    @JsonIgnore
    private LoanDetails.Type getLoanType() {
        if (isHousingLoan()) {
            return LoanDetails.Type.MORTGAGE;
        } else if (isCarLoan) {
            return LoanDetails.Type.VEHICLE;
        }
        return LoanDetails.Type.OTHER;
    }

    @JsonIgnore
    private boolean isHousingLoan() {
        return isHomeLoan || isFixedInterestLoan || isTopplaan || isFlexiLoan || isByggelaan;
    }
}
