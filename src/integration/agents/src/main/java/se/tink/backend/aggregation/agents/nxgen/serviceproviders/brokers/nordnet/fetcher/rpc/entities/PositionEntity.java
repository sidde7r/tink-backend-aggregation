package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class PositionEntity {
    private static final Logger log = LoggerFactory.getLogger(PositionEntity.class);

    @JsonProperty("accno")
    private String accountNumber;

    @JsonProperty("accid")
    private String accountId;

    private InstrumentEntity instrument;

    @JsonProperty("main_market_price")
    private AmountEntity mainMarketPrice;

    @JsonProperty("morning_price")
    private AmountEntity morningPrice;

    @JsonProperty("qty")
    private double quantity;

    @JsonProperty("pawn_percent")
    private int pawnPercentage;

    @JsonProperty("market_value_acc")
    private AmountEntity accountCurrencyMarketValue;

    @JsonProperty("market_value")
    private AmountEntity marketValue;

    @JsonProperty("acq_price")
    private AmountEntity acquisitionPrice;

    @JsonProperty("acq_price_acc")
    private AmountEntity accountCurrencyAcquisitionPrice;

    @JsonProperty("is_custom_gav")
    private boolean isCustomerGav;

    private InstrumentModule.InstrumentType getType() {
        if (getInstrument() == null || getInstrument().getGroupType() == null) {
            return InstrumentModule.InstrumentType.OTHER;
        }

        switch (getInstrument().getGroupType().toLowerCase()) {
            case "eq":
                return InstrumentModule.InstrumentType.STOCK;
            case "fnd":
                return InstrumentModule.InstrumentType.FUND;
            default:
                return InstrumentModule.InstrumentType.OTHER;
        }
    }

    private InstrumentIdModule getIdModule() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(instrument.getIsin())
                .withName(instrument.getName())
                .setIsin(instrument.getIsin())
                .setMarketPlace(instrument.getInstitute())
                .build();
    }

    public Optional<InstrumentModule> toTinkInstrument() {
        return Optional.of(
                InstrumentModule.builder()
                        .withType(getType())
                        .withId(getIdModule())
                        .withMarketPrice(mainMarketPrice.getValue().doubleValue())
                        .withMarketValue(marketValue.getValue().doubleValue())
                        .withAverageAcquisitionPrice(acquisitionPrice.getValue().doubleValue())
                        .withCurrency(instrument.getCurrency())
                        .withQuantity(quantity)
                        .withProfit(getProfit())
                        .setRawType(
                                String.format(
                                        "type: %s, group type: %s",
                                        instrument.getType(), instrument.getGroupType()))
                        .setTicker(instrument.getSymbol())
                        .build());
    }

    private double getProfit() {

        if (getMarketValue() == null
                || getAcquisitionPrice() == null
                || getMarketValue().getValue() == null
                || getAcquisitionPrice().getValue() == null) {
            log.warn("Not receiving marketValue or acquisitionPrice");
            return 0.0;
        }

        return ExactCurrencyAmount.inSEK(
                        getMarketValue().getValue().doubleValue()
                                - (getQuantity() * getAcquisitionPrice().getValue().doubleValue()))
                .getDoubleValue();
    }
}
