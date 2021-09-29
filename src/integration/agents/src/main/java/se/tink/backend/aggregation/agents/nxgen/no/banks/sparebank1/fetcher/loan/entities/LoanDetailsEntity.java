package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanDetailsEntity {
    private String actualInterestRateInteger;
    private String actualInterestRateFraction;
    private String ownerFirstName;
    private String ownerLastName;
    private String loanAmountInteger;
    private String loanAmountFraction;
    private String balanceAmountInteger;
    private String balanceAmountFraction;
    private Integer period;
    private String type;

    @JsonProperty("discountingDate")
    private LocalDate initialDate;

    @JsonProperty("maturityDate")
    private LocalDate endLoanDate;

    private Installment installment;

    @Getter
    @JsonObject
    public static class Installment {
        private String amountInteger;
        private String amountFraction;
    }

    @JsonIgnore
    Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(
                parseInterestRate(actualInterestRateInteger)
                        + ","
                        + parseInterestRate(actualInterestRateFraction));
    }

    Double getBalance() {
        return Sparebank1AmountUtils.constructDouble(balanceAmountInteger, balanceAmountFraction);
    }

    Double getInitialBalance() {
        return Sparebank1AmountUtils.constructDouble(loanAmountInteger, loanAmountFraction);
    }

    Double getAmortized() {
        return BigDecimal.valueOf(getInitialBalance())
                .subtract(BigDecimal.valueOf(getBalance()))
                .doubleValue();
    }

    List<String> getApplicants() {
        return ImmutableList.of(ownerFirstName + " " + ownerLastName);
    }

    Integer getNumMonthsBounds() {
        if (period == null) {
            return null;
        }

        if (type.equals("ANNUITY")) {
            return period * 12;
        }
        return period;
    }

    Double getMonthlyAmortization() {
        if (installment == null) {
            return null;
        }
        return Sparebank1AmountUtils.constructDouble(
                installment.getAmountInteger(), installment.getAmountFraction());
    }

    private String parseInterestRate(String interestRate) {
        return Strings.isNullOrEmpty(interestRate) ? "0" : interestRate;
    }
}
