package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class PortfolioEntity {
    private String name;
    private String id;
    private Boolean tradable;
    private Boolean allowsBuy;
    private Boolean allowsSell;
    private Boolean shared;
    private String totalMarketValueInteger;
    private String totalMarketValueFraction;
    private String totalCostPriceInteger;
    private String totalCostPriceFraction;
    private String totalRateOfInvestCurrencyInteger;
    private String totalRateOfInvestCurrencyFraction;
    private String totalRateOfInvestPercentageInteger;
    private String totalRateOfInvestPercentageFraction;
    private Boolean vpsAccount;
    private Boolean askPortfolio;
    private String status;
    private Boolean periodicReportsIncluded;
    private Boolean owner;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Boolean getTradable() {
        return tradable;
    }

    public Boolean getAllowsBuy() {
        return allowsBuy;
    }

    public Boolean getAllowsSell() {
        return allowsSell;
    }

    public Boolean getShared() {
        return shared;
    }

    public String getTotalMarketValueInteger() {
        return totalMarketValueInteger;
    }

    public String getTotalMarketValueFraction() {
        return totalMarketValueFraction;
    }

    public String getTotalCostPriceInteger() {
        return totalCostPriceInteger;
    }

    public String getTotalCostPriceFraction() {
        return totalCostPriceFraction;
    }

    public String getTotalRateOfInvestCurrencyInteger() {
        return totalRateOfInvestCurrencyInteger;
    }

    public String getTotalRateOfInvestCurrencyFraction() {
        return totalRateOfInvestCurrencyFraction;
    }

    public String getTotalRateOfInvestPercentageInteger() {
        return totalRateOfInvestPercentageInteger;
    }

    public String getTotalRateOfInvestPercentageFraction() {
        return totalRateOfInvestPercentageFraction;
    }

    public Boolean getVpsAccount() {
        return vpsAccount;
    }

    public Boolean getAskPortfolio() {
        return askPortfolio;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getPeriodicReportsIncluded() {
        return periodicReportsIncluded;
    }

    public Boolean getOwner() {
        return owner;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    private Double getTotalMarketValue() {
        if (Strings.isNullOrEmpty(totalMarketValueInteger) || Strings.isNullOrEmpty(totalMarketValueFraction)) {
            return 0.0;
        }

        return Sparebank1AmountUtils.constructDouble(totalMarketValueInteger, totalMarketValueFraction);
    }

    private Double getTotalProfit() {
        if (Strings.isNullOrEmpty(totalRateOfInvestCurrencyInteger) ||
                Strings.isNullOrEmpty(totalRateOfInvestCurrencyFraction)) {
            return 0.0;
        }

        // totalRateOfInvestCurrency is the diff between totalMarketValue and totalCostPrice
        return Sparebank1AmountUtils.constructDouble(totalRateOfInvestCurrencyInteger,
                totalRateOfInvestCurrencyFraction);
    }

    public InvestmentAccount toAccount(Portfolio portfolio) {
        return InvestmentAccount.builder(id)
                .setAccountNumber(id)
                .setName(name)
                .setCashBalance(Amount.inNOK(0))
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(id);
        portfolio.setTotalValue(getTotalMarketValue());
        portfolio.setRawType(name);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalProfit(getTotalProfit());

        return portfolio;
    }
}
