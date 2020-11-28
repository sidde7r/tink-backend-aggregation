package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@Getter
@JsonObject
public class FundListEntity {
    @JsonProperty("antalAndelar")
    private Double numberOfShares;

    @JsonProperty("avgiftProcent")
    private Double feePercent;

    @JsonProperty("kurs")
    private Double rate;

    @JsonProperty("namn")
    private String name;

    @JsonProperty("nummer")
    private int number;

    @JsonProperty("varde")
    private Double value;

    @JsonProperty("vardedatum")
    private String valueDate;

    public InstrumentModule toTinkInstrument() {
        return InstrumentModule.builder()
                .withType(InstrumentType.FUND)
                .withId(
                        InstrumentIdModule.builder()
                                .withUniqueIdentifier(Integer.toString(number))
                                .withName(name)
                                .build())
                .withMarketPrice(rate)
                .withMarketValue(value)
                .withAverageAcquisitionPrice(null) // Not available in response
                .withCurrency(MinPensionConstants.CURRENCY_SEK)
                .withQuantity(numberOfShares)
                .withProfit(value)
                .build();
    }
}
