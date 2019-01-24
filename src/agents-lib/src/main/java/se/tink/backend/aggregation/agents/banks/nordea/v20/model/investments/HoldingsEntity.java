package se.tink.backend.aggregation.agents.banks.nordea.v20.model.investments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public Double getAvgPurchasePrice() {
        return avgPurchasePrice == null || avgPurchasePrice.isEmpty() ? null : StringUtils.parseAmount(avgPurchasePrice);
    }

    public void setAvgPurchasePrice(String avgPurchasePrice) {
        this.avgPurchasePrice = avgPurchasePrice;
    }

    public Double getProfit() {
        return profit == null || profit.isEmpty() ? null : StringUtils.parseAmount(profit);
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public Double getQuantity() {
        return quantity == null ? null : StringUtils.parseAmount(quantity);
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public ExchangeRateEntity getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(
            ExchangeRateEntity exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public InstrumentEntity getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentEntity instrument) {
        this.instrument = instrument;
    }

    public Optional<Instrument> toInstrument(String baseCurrency) {
        Instrument instrument = new Instrument();


        if (getQuantity() == null || getQuantity().doubleValue() == 0) {
            return Optional.empty();
        }

        InstrumentEntity thisInstrument = getInstrument();
        String isin = thisInstrument.getInstrumentId().getIsin();
        String market = thisInstrument.getInstrumentId().getMarket();

        instrument.setAverageAcquisitionPrice(getAvgPurchasePrice());
        instrument.setCurrency(baseCurrency);
        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setMarketValue(getMarketValue());
        instrument.setName(thisInstrument.getInstrumentName());
        instrument.setPrice(thisInstrument.getPrice());
        instrument.setProfit(getProfit());
        instrument.setQuantity(getQuantity());
        instrument.setRawType(thisInstrument.getInstrumentType());
        instrument.setType(thisInstrument.getTinkInstrumentType());
        instrument.setUniqueIdentifier(isin + market);

        return Optional.of(instrument);
    }
}
