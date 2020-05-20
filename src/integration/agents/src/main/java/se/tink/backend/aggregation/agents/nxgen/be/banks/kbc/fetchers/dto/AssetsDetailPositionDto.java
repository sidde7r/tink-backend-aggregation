package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class AssetsDetailPositionDto {

    private static final Double ZERO = Double.valueOf(0);

    private TypeValuePair groupId;
    private TypeValuePair presentation;
    private TypeValuePair currentValue;
    private TypeValuePair accruedInterest;
    private TypeValuePair productName;
    private TypeValuePair productType;
    private TypeValuePair enrichedName;
    private TypeValuePair marketValue;
    private TypeValuePair marketValueCurrency;
    private TypeValuePair agreementNumber;
    private TypeValuePair notRealisedPercentage;
    private TypeValuePair notRealisedValue;
    private TypeValuePair notRealisedValueCurrency;
    private TypeValuePair numberValue;
    private TypeValuePair presentIndicativeRate;
    private TypeValuePair presentRateCurrency;
    private TypeValuePair presentRateDate;
    private TypeValuePair noPersonalAdvice;
    private TypeValuePair positionTimestamp;
    private TypeValuePair insuranceCashProduct;

    public InstrumentModule toTinkInstrument(InstrumentModule.InstrumentType type) {
        return InstrumentModule.builder()
                .withType(type)
                .withId(toTinkInstrumentId())
                .withMarketPrice(toMarketPrice())
                .withMarketValue(toMarketValue())
                .withAverageAcquisitionPrice(toAverageAcquisitionPrice())
                .withCurrency(
                        Stream.of(
                                        marketValueCurrency,
                                        notRealisedValueCurrency,
                                        presentRateCurrency)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .map(TypeValuePair::getValue)
                                .orElse("EUR"))
                .withQuantity(toQuantity())
                .withProfit(toProfit())
                .build();
    }

    private InstrumentIdModule toTinkInstrumentId() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier("Subgroup" + toPositionId())
                .withName(Optional.ofNullable(productName).map(TypeValuePair::getValue).orElse(""))
                .build();
    }

    public String toProductName() {
        return Optional.ofNullable(productName).map(TypeValuePair::getValue).orElse("");
    }

    public Double toMarketPrice() {
        return Optional.ofNullable(presentIndicativeRate)
                .map(TypeValuePair::getValue)
                .map(Double::valueOf)
                .orElse(ZERO);
    }

    public Double toMarketValue() {
        return Optional.ofNullable(marketValue)
                .map(TypeValuePair::getValue)
                .map(Double::valueOf)
                .orElse(ZERO);
    }

    public Double toProfit() {
        return Optional.ofNullable(notRealisedValue)
                .map(TypeValuePair::getValue)
                .map(Double::valueOf)
                .orElse(ZERO);
    }

    private Double toQuantity() {
        return Optional.ofNullable(numberValue)
                .map(TypeValuePair::getValue)
                .map(Double::valueOf)
                .orElse(ZERO);
    }

    private Double toAverageAcquisitionPrice() {
        Double quantity = toQuantity();
        if (quantity.equals(ZERO)) {
            return ZERO;
        }
        Double aap = (toMarketValue() - toProfit()) / toQuantity();
        if (aap.compareTo(ZERO) < 0) {
            return ZERO;
        }
        return aap;
    }

    public String toPositionId() {
        return Optional.ofNullable(groupId).map(TypeValuePair::getValue).orElse("");
    }
}
