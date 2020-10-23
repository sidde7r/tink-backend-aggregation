package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class InstrumentEntity {
    @JsonProperty private String id;
    @JsonProperty private double price;
    @JsonProperty private String currency;
    @JsonProperty private String market;

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("instrument_type")
    private String rawType;

    @JsonProperty private String isin;

    @JsonIgnore
    public InstrumentModule applyTo(
            double marketValue, double profitLoss, double quantity, double avgPurchasePrice) {

        return InstrumentModule.builder()
                .withType(NordeaSEConstants.INSTRUMENT_TYPE_MAP.translate(rawType).get())
                .withId(InstrumentIdModule.of(isin, getMarket(), instrumentName, id))
                .withMarketPrice(price)
                .withMarketValue(marketValue)
                .withAverageAcquisitionPrice(avgPurchasePrice)
                .withCurrency(currency)
                .withQuantity(quantity)
                .withProfit(profitLoss)
                .setRawType(rawType)
                .build();
    }

    @JsonIgnore
    public String getRawType() {
        return rawType;
    }

    public String getMarket() {
        return Optional.ofNullable(market).orElse(extractMarketFromId());
    }

    private String extractMarketFromId() {
        // id format IMC-SE000XXXXXX-MARKET-SEK
        String[] idParts = id.split("-");
        if (idParts.length == 4) {
            return idParts[2];
        } else {
            throw new NoSuchElementException("can't determine instrument market");
        }
    }
}
