package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonObject
public class HoldingsEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String marketValue;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String avgPurchasePrice;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    @JsonProperty("profitLoss")
    private String profit;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
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
