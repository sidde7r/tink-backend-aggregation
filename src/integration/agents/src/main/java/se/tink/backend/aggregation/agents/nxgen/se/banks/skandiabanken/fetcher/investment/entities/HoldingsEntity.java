package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HoldingsEntity {
    @JsonProperty("AcquisitionValue")
    private BigDecimal acquisitionValue;

    @JsonProperty("Currency")
    private String currency = "";

    @JsonProperty("CurrentValue")
    private BigDecimal currentValue;

    @JsonProperty("CurrentCalculationTime")
    private String currentCalculationTime = "";

    @JsonProperty("ExchangeRate")
    private BigDecimal exchangeRate;

    @JsonProperty("Instrument")
    private InstrumentEntity instrument;

    @JsonProperty("IsgCode")
    private String isgCode = "";

    @JsonProperty("ISIN")
    private String isin = "";

    @JsonProperty("Name")
    private String name = "";

    @JsonProperty("PendingReturn")
    private BigDecimal pendingReturn;

    @JsonProperty("PendingPercentageReturn")
    private BigDecimal pendingPercentageReturn;

    @JsonProperty("Shares")
    private BigDecimal shares;

    @JsonProperty("ShortName")
    private String shortName = "";

    @JsonProperty("TodaysRate")
    private BigDecimal todaysRate;

    @JsonProperty("HasUnseenDeprecation")
    private boolean hasUnseenDeprecation;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setInstrument(InstrumentEntity instrument) {
        this.instrument = instrument;
    }

    @JsonIgnore
    private InstrumentEntity getInstrument() {
        return instrument;
    }

    @JsonIgnore
    public Instrument toTinkInstrument() {
        Instrument tinkInstrument = getInstrument().toTinkInstrument(todaysRate);
        tinkInstrument.setAverageAcquisitionPrice(
                acquisitionValue.divide(shares, BigDecimal.ROUND_HALF_UP).doubleValue());
        tinkInstrument.setQuantity(shares.doubleValue());
        tinkInstrument.setProfit(
                currentValue != null
                        ? currentValue.doubleValue() - acquisitionValue.doubleValue()
                        : null);
        tinkInstrument.setMarketValue(
                currentValue != null
                        ? currentValue.doubleValue()
                        : todaysRate.multiply(shares).doubleValue());

        return tinkInstrument;
    }
}
