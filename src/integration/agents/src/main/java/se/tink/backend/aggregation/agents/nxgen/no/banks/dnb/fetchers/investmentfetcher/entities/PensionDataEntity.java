package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PensionDataEntity {
    private double totalHolding;
    private double currentYearInvestment;
    private double allowedSavingsPerYear;
    private double currentYearTaxSaving;
    private double totalNotSettledInvestment;
    private List<String> accountNumbers;
    private String levMerProductId;
    private String levMerProductSystem;
    private boolean levMerAlreadyPurchased;
    private int ipsHoldingsCount;
    private boolean ipsEligible;
    private boolean ipsCustomer;

    public double getTotalHolding() {
        return totalHolding;
    }

    public double getCurrentYearInvestment() {
        return currentYearInvestment;
    }

    public double getAllowedSavingsPerYear() {
        return allowedSavingsPerYear;
    }

    public double getCurrentYearTaxSaving() {
        return currentYearTaxSaving;
    }

    public double getTotalNotSettledInvestment() {
        return totalNotSettledInvestment;
    }

    public List<String> getAccountNumbers() {
        return accountNumbers;
    }

    public String getLevMerProductId() {
        return levMerProductId;
    }

    public String getLevMerProductSystem() {
        return levMerProductSystem;
    }

    public boolean isLevMerAlreadyPurchased() {
        return levMerAlreadyPurchased;
    }

    public int getIpsHoldingsCount() {
        return ipsHoldingsCount;
    }

    public boolean isIpsEligible() {
        return ipsEligible;
    }

    public boolean isIpsCustomer() {
        return ipsCustomer;
    }

    public Portfolio toTinkPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(accountNumbers.get(0));
        portfolio.setRawType(levMerProductSystem + "-" + levMerProductId);
        portfolio.setType(Portfolio.Type.PENSION);
        portfolio.setTotalValue(totalHolding);

        return portfolio;
    }

    public InvestmentAccount toTinkAccount() {
        return InvestmentAccount.builder(accountNumbers.get(0))
                .setAccountNumber(accountNumbers.get(0))
                .setName("Pension")
                .setCashBalance(ExactCurrencyAmount.zero("NOK"))
                .setPortfolios(Collections.singletonList(toTinkPortfolio()))
                .build();
    }
}
