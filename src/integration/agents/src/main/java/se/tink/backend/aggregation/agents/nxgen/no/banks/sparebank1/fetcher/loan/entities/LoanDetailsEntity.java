package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

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
    private int period;
    private String type;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonProperty("discountingDate")
    private LocalDate initialDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
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
    public Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(
                actualInterestRateInteger + "," + actualInterestRateFraction);
    }

    public Double getBalance() {
        return Sparebank1AmountUtils.constructDouble(balanceAmountInteger, balanceAmountFraction);
    }

    public Double getInitialBalance() {
        return Sparebank1AmountUtils.constructDouble(loanAmountInteger, loanAmountFraction);
    }

    public Double getAmortized() {
        return getInitialBalance() - getBalance();
    }

    public List<String> getAplicants() {
        return ImmutableList.of(ownerFirstName + " " + ownerLastName);
    }

    public int getNumMonthsOfBouds() {
        if (type.equals("ANNUITY")) {
            return period * 12;
        }
        return period;
    }

    public Double getMonthlyAmortization() {
        return Sparebank1AmountUtils.constructDouble(
                installment.getAmountInteger(), installment.getAmountFraction());
    }
}
