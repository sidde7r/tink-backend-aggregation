package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CustodyHoldingEntity {

    private double avgPurchasePrice;
    private String currency;
    private String id;
    private InstrumentEntity instrument;
    private double marketValue;
    private String name;

    @JsonProperty("profit_loss")
    private double profit;

    @JsonProperty("profit_loss_percentage")
    private double profitPercentage;

    private double purchaseValue;

    private double quantity;

    public Optional<InstrumentModule> toInstrument() {
        if (instrument.isCash()) {
            return Optional
                    .empty(); // if it's cash it's not an instrument, it's already included in cash
            // value
        }
        InstrumentIdBuildStep builder =
                InstrumentIdModule.builder().withUniqueIdentifier(id).withName(name);
        if (instrument.isFund()) {
            builder = builder.setIsin(instrument.getIsin());
        }
        InstrumentIdModule idModule = builder.build();
        return Optional.of(
                InstrumentModule.builder()
                        .withType(type())
                        .withId(idModule)
                        .withMarketPrice(instrument.getPrice())
                        .withMarketValue(marketValue)
                        .withAverageAcquisitionPrice(avgPurchasePrice)
                        .withCurrency(currency)
                        .withQuantity(quantity)
                        .withProfit(profit)
                        .setRawType(instrument.getRawType())
                        .build());
    }

    InstrumentModule.InstrumentType type() {
        if (instrument.isFund()) {
            return InstrumentModule.InstrumentType.FUND;
        }
        if (instrument.isDerivative() || instrument.isEquity()) {
            return InstrumentModule.InstrumentType.STOCK;
        }
        return InstrumentModule.InstrumentType.OTHER;
    }
}
