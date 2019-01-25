package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.PortfolioTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

@JsonObject
public class PortfolioEntity {
    @JsonProperty("instrumentPositions")
    private List<InstrumentEntity> instruments;

    private double totalProfit;
    private double totalOwnCapital;
    private double totalBuyingPower;
    private double totalProfitPercent;
    private double totalBalance;
    private String accountName;
    private String accountType;
    private boolean depositable;
    private String accountId;

    public List<InstrumentEntity> getInstruments() {
        return Optional.ofNullable(instruments).orElse(Collections.emptyList());
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getTotalOwnCapital() {
        return totalOwnCapital;
    }

    public double getTotalBuyingPower() {
        return totalBuyingPower;
    }

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isDepositable() {
        return depositable;
    }

    public String getAccountId() {
        return accountId;
    }

    @JsonIgnore
    public Portfolio toTinkPortfolio(List<Instrument> instruments) {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(accountType);
        portfolio.setType(getPortfolioType());
        portfolio.setCashValue(totalBuyingPower);
        portfolio.setTotalValue(totalOwnCapital);
        portfolio.setTotalProfit(totalProfit);
        portfolio.setUniqueIdentifier(accountId);
        portfolio.setInstruments(instruments);

        return portfolio;
    }

    public InvestmentAccount toTinkInvestmentAccount(
            HolderName holderName, List<Instrument> instruments) {
        return toTinkInvestmentAccount(holderName, toTinkPortfolio(instruments));
    }

    public InvestmentAccount toTinkInvestmentAccount(HolderName holderName, Portfolio portfolio) {
        return InvestmentAccount.builder(getAccountId())
                .setAccountNumber(getAccountId())
                .setName(getAccountType())
                .setHolderName(holderName)
                .setCashBalance(Amount.inSEK(getTotalBuyingPower()))
                .setBankIdentifier(getAccountId())
                .setPortfolios(Lists.newArrayList(portfolio))
                .build();
    }

    @JsonIgnore
    private Portfolio.Type getPortfolioType() {
        switch (getAccountType().toLowerCase()) {
            case PortfolioTypes.INVESTERINGSSPARKONTO:
                return Portfolio.Type.ISK;
            case PortfolioTypes.AKTIEFONDKONTO:
                return Portfolio.Type.DEPOT;
            case PortfolioTypes.TJANSTEPENSION:
            case PortfolioTypes.PENSIONSFORSAKRING:
            case PortfolioTypes.IPS:
                return Portfolio.Type.PENSION;
            case PortfolioTypes.KAPITALFORSAKRING:
                return Portfolio.Type.KF;
            default:
                return Portfolio.Type.OTHER;
        }
    }
}
