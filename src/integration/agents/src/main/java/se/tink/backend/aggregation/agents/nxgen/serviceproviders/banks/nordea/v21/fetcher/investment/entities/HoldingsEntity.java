package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class HoldingsEntity {
    private String marketValue;
    private String avgPurchasePrice;
    @JsonProperty("profitLoss")
    private String profit;
    private String quantity;
    private ExchangeRateEntity exchangeRate;
    private InstrumentEntity instrument;

    public Double getMarketValue() {
        return marketValue == null || marketValue.isEmpty() ? null : StringUtils.parseAmount(marketValue);
    }

    public Double getAvgPurchasePrice() {
        return avgPurchasePrice == null || avgPurchasePrice.isEmpty() ? null : StringUtils.parseAmount(avgPurchasePrice);
    }

    public Double getProfit() {
        return profit == null || profit.isEmpty() ? null : StringUtils.parseAmount(profit);
    }

    public Double getQuantity() {
        return quantity == null ? null : StringUtils.parseAmount(quantity);
    }

    public ExchangeRateEntity getExchangeRate() {
        return exchangeRate;
    }

    public InstrumentEntity getInstrument() {
        return instrument;
    }
}
