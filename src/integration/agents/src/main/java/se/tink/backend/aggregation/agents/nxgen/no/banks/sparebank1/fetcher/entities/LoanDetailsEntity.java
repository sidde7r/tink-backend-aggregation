package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanDetailsEntity {
    private String id;
    private String name;
    private String formattedNumber;
    private String disposableAmountInteger;
    private String disposableAmountFraction;
    private String balanceAmountInteger;
    private String balanceAmountFraction;
    private String type;
    private String loanAmountInteger;
    private String loanAmountFraction;
    private String interestRateInteger;
    private String interestRateFraction;
    private String actualInterestRateInteger;
    private String actualInterestRateFraction;
    private String discountingDate;
    private Integer period;
    private String periodExpensesInteger;
    private String periodExpensesFraction;
    private String invoiceFlag;
    private String maturityDate;
    private String collateral;
    private InstallmentEntity installment;
    private Boolean balancePreferred;
    private Boolean transferFromEnabled;
    private Boolean transferToEnabled;
    private Boolean paymentFromEnabled;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public String getDisposableAmountInteger() {
        return disposableAmountInteger;
    }

    public String getDisposableAmountFraction() {
        return disposableAmountFraction;
    }

    public String getBalanceAmountInteger() {
        return balanceAmountInteger;
    }

    public String getBalanceAmountFraction() {
        return balanceAmountFraction;
    }

    public String getType() {
        return type;
    }

    public String getLoanAmountInteger() {
        return loanAmountInteger;
    }

    public String getLoanAmountFraction() {
        return loanAmountFraction;
    }

    public String getInterestRateInteger() {
        return interestRateInteger;
    }

    public String getInterestRateFraction() {
        return interestRateFraction;
    }

    public String getActualInterestRateInteger() {
        return actualInterestRateInteger;
    }

    public String getActualInterestRateFraction() {
        return actualInterestRateFraction;
    }

    public String getDiscountingDate() {
        return discountingDate;
    }

    public Integer getPeriod() {
        return period;
    }

    public String getPeriodExpensesInteger() {
        return periodExpensesInteger;
    }

    public String getPeriodExpensesFraction() {
        return periodExpensesFraction;
    }

    public String getInvoiceFlag() {
        return invoiceFlag;
    }

    public String getMaturityDate() {
        return maturityDate;
    }

    public String getCollateral() {
        return collateral;
    }

    public InstallmentEntity getInstallment() {
        return installment;
    }

    public Boolean getBalancePreferred() {
        return balancePreferred;
    }

    public Boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public Boolean getTransferToEnabled() {
        return transferToEnabled;
    }

    public Boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    @JsonIgnore
    public Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(
                interestRateInteger + "," + interestRateFraction);
    }

    @JsonIgnore
    public Amount getInitialBalance() {
        return Sparebank1AmountUtils.constructAmount(loanAmountInteger, loanAmountFraction);
    }
}
