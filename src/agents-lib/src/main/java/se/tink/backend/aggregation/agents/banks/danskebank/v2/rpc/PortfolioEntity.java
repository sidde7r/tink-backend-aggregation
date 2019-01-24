package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioEntity {
    @JsonProperty("Change")
    private Double change;
    @JsonProperty("ChangeValue")
    private Double changeValue;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("PortfolioId")
    private String portfolioId;
    @JsonProperty("PortfolioName")
    private String portfolioName;
    @JsonProperty("PortfolioSimpleId")
    private String portfolioSimpleId;
    @JsonProperty("PortfolioType")
    private String portfolioType;
    @JsonProperty("TotalValue")
    private Double totalValue;

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(Double changeValue) {
        this.changeValue = changeValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public String getPortfolioSimpleId() {
        return portfolioSimpleId;
    }

    public void setPortfolioSimpleId(String portfolioSimpleId) {
        this.portfolioSimpleId = portfolioSimpleId;
    }

    public String getPortfolioType() {
        return portfolioType;
    }

    public void setPortfolioType(String portfolioType) {
        this.portfolioType = portfolioType;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(getPortfolioId());
        account.setBalance(getTotalValue());
        account.setBankId(getPortfolioId());
        account.setName(getPortfolioName());
        account.setType(AccountTypes.INVESTMENT);


        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        Portfolio.Type type = DanskeUtils.PORTFOLIO_TYPE_MAPPER
                .translate(getPortfolioType())
                .orElse(Portfolio.Type.DEPOT);

        portfolio.setRawType(getPortfolioType());
        portfolio.setTotalProfit(getChangeValue());
        portfolio.setTotalValue(getTotalValue());
        portfolio.setType(type);
        portfolio.setUniqueIdentifier(getPortfolioId());

        return portfolio;
    }
}
