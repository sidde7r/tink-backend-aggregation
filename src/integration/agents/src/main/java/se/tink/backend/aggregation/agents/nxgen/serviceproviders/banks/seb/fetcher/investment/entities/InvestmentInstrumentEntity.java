package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class InvestmentInstrumentEntity {
    @JsonProperty("ANDEL_ANTAL")
    private double shares;

    @JsonProperty("ANDEL_BELOPP")
    private double currentValue;

    @JsonProperty("ANSKAFF_KOSTNAD")
    private double acquisitionPrice;

    @JsonProperty("FOND_KURS_BELOPP")
    private double marketPrice;

    @JsonProperty("FOND_NAMN_SMA")
    private String name;

    @JsonProperty("FORS_NR")
    private String id;

    public InstrumentModule toTinkInstrument() {
        InstrumentIdModule idModule =
                InstrumentIdModule.builder().withUniqueIdentifier(id).withName(name.trim()).build();
        return InstrumentModule.builder()
                .withType(InstrumentModule.InstrumentType.FUND)
                .withId(idModule)
                .withMarketPrice(marketPrice)
                .withMarketValue(currentValue)
                .withAverageAcquisitionPrice(acquisitionPrice)
                .withCurrency(SebConstants.DEFAULT_CURRENCY)
                .withQuantity(shares)
                .withProfit(income())
                .build();
    }

    private double income() {
        return currentValue - acquisitionPrice;
    }
}
