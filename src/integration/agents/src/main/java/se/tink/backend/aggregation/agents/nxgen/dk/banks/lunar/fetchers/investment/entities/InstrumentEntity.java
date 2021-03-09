package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
@Slf4j
public class InstrumentEntity {
    private String baseCurrency;
    private BigDecimal currentPriceInBaseCurrency;
    @Getter private Boolean deleted;
    private String id;
    private String tickerCode;
    private String title;
    private String type;
    @Getter private PositionEntity position;

    @JsonIgnore
    public InstrumentModule toInstrument() {
        return InstrumentModule.builder()
                .withType(getType())
                .withId(buildInstrumentIdModule())
                .withMarketPrice(currentPriceInBaseCurrency.doubleValue())
                .withMarketValue(currentPriceInBaseCurrency.doubleValue())
                .withAverageAcquisitionPrice(position.getAveragePriceInBaseCurrency())
                .withCurrency(baseCurrency)
                .withQuantity(position.getAmount().doubleValue())
                .withProfit(position.getProfitLossOnTradeInBaseCurrency())
                .setRawType(type)
                .setTicker(tickerCode)
                .build();
    }

    public BigDecimal calculateInstrumentValue() {
        return currentPriceInBaseCurrency.multiply(position.getAmount());
    }

    private InstrumentModule.InstrumentType getType() {
        if (StringUtils.isNotBlank(type) && "stock".equals(type)) {
            return InstrumentModule.InstrumentType.STOCK;
        }
        log.info("Found different Lunar instrument type than Stock!");
        return InstrumentModule.InstrumentType.OTHER;
    }

    private InstrumentIdModule buildInstrumentIdModule() {
        return InstrumentIdModule.builder().withUniqueIdentifier(id).withName(title).build();
    }
}
