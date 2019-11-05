package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentResultEntity {
    private final String name;
    private final String currency;
    private final String isin;
    private final double executedPrice;
    private final double quantity;
    private final double executedValue;
    private final double lastPrice;
    private final double profitPercentages;
    private final double profit;
    private final double totalValue;

    public InvestmentResultEntity(
            StocksItemEntity stocksItemEntity,
            StockPriceEntity stockPriceEntity,
            HoldingsItemEntity holdingsItemEntity) {
        this.name = stocksItemEntity.getName();
        this.isin = stocksItemEntity.getIsin();
        this.currency = holdingsItemEntity.getCurrency();

        this.executedPrice =
                holdingsItemEntity.getAveragePriceEntity().getAmount()
                        / RevolutConstants.REVOLUT_AMOUNT_DIVIDER;
        this.quantity = Double.parseDouble(holdingsItemEntity.getBalanceEntity().getQuantity());
        this.executedValue = executedPrice * quantity;
        this.lastPrice = stockPriceEntity.getLast() / RevolutConstants.REVOLUT_AMOUNT_DIVIDER;
        this.profitPercentages = (lastPrice / executedPrice) - 1;
        this.profit = executedValue * profitPercentages;
        this.totalValue = executedPrice * quantity;
    }

    public String getName() {
        return name;
    }

    public String getIsin() {
        return isin;
    }

    public double getExecutedPrice() {
        return executedPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getExecutedValue() {
        return executedValue;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getProfitPercentages() {
        return profitPercentages;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public String getCurrency() {
        return currency;
    }

    public double getProfit() {
        return profit;
    }
}
