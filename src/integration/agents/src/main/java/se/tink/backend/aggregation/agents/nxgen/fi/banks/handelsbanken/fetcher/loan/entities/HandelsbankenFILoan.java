package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class HandelsbankenFILoan {
    private String loanName;
    private String loanNumber;
    private HandelsbankenAmount loanAmount;
    private String totalInterestRate;
    private HandelsbankenAmount amountToPay;
    private boolean displayBadge;
    private Object loanDetails;

    @JsonIgnore
    public LoanDetails.Type getType() {
        return HandelsbankenFIConstants.LoanType.findLoanType(loanName).getTinkType();
    }

    @JsonIgnore
    public double getInterest() {
        if (totalInterestRate != null) {
            String interestRateString = totalInterestRate.replace("%", "");
            return StringUtils.parseAmount(interestRateString);
        }

        return 0;
    }

    public String getLoanName() {
        return loanName;
    }

    @JsonIgnore
    public LoanAccount toLoanAccount() {
        LoanDetails details = LoanDetails
                .builder(HandelsbankenFIConstants.LoanType.findLoanType(loanName).getTinkType())
                .setLoanNumber(loanNumber)
                .build();

        if (loanAmount.asDouble() > 0 ) {
            loanAmount.setAmount(-loanAmount.asDouble());
        }

        return LoanAccount.builder(loanNumber, loanAmount.asAmount())
                .setAccountNumber(loanNumber)
                .setName(loanName)
                .setInterestRate(getInterest())
                .setDetails(details)
                .setBankIdentifier(loanNumber)
                .build();
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }

}
