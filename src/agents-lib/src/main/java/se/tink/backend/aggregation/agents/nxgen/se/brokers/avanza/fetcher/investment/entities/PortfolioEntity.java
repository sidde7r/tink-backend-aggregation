package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
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
    public List<Instrument> toTinkInstruments(
            IsinMap isinMap, AvanzaApiClient apiClient, String session) {
        return getInstruments()
                .stream()
                .flatMap(
                        instrument ->
                                instrument
                                        .getPositions()
                                        .stream()
                                        .map(
                                                position -> {
                                                    final String instrumentType =
                                                            instrument.getInstrumentType();
                                                    final String orderbookId =
                                                            position.getOrderbookId();
                                                    final String market =
                                                            apiClient.getInstrumentMarket(
                                                                    instrumentType,
                                                                    orderbookId,
                                                                    session);

                                                    return position.toTinkInstrument(
                                                            instrument, market, isinMap);
                                                }))
                .collect(Collectors.toList());
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

    @JsonIgnore
    private Portfolio.Type getPortfolioType() {
        switch (getAccountType().toLowerCase()) {
            case "investeringssparkonto":
                return Portfolio.Type.ISK;
            case "aktiefondkonto":
                return Portfolio.Type.DEPOT;
            case "tjanstepension":
            case "pensionsforsakring":
            case "ips":
                return Portfolio.Type.PENSION;
            case "kapitalforsakring":
                return Portfolio.Type.KF;
            default:
                return Portfolio.Type.OTHER;
        }
    }
}
