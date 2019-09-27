package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities;

import static se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.CURRENCY;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.INSTRUMENT_TYPE_MAP;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class PositionDetailsEntity {
    @JsonProperty private BigDecimal amount;
    @JsonProperty private String category = "";
    @JsonProperty private BigDecimal change;
    @JsonProperty private BigDecimal changePercent;
    @JsonProperty private Boolean investingAllowed;
    @JsonProperty private String isin = "";
    @JsonProperty private BigDecimal marketValue;
    @JsonProperty private int openAssignments;
    @JsonProperty private PortfolioEntity portfolio;
    @JsonProperty private String positionName = "";
    @JsonProperty private BigDecimal purchaseValue;
    @JsonProperty private BigDecimal singleMarketPrice;

    @JsonIgnore
    public InstrumentModule toTinkInstrument(String securityId) {
        return InstrumentModule.builder()
                .withType(getType())
                .withId(getInstrumentIdModule(securityId))
                .withMarketPrice(singleMarketPrice.doubleValue())
                .withMarketValue(marketValue.doubleValue())
                .withAverageAcquisitionPrice(
                        purchaseValue.divide(amount, 3, RoundingMode.HALF_UP).doubleValue())
                .withCurrency(CURRENCY)
                .withQuantity(amount.doubleValue())
                .withProfit(change.doubleValue())
                .setRawType(category)
                .build();
    }

    @JsonIgnore
    private InstrumentIdModule getInstrumentIdModule(String securityId) {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(securityId)
                .withName(positionName)
                .setIsin(isin)
                .build();
    }

    @JsonIgnore
    private InstrumentType getType() {
        return INSTRUMENT_TYPE_MAP.translate(category).orElse(InstrumentType.OTHER);
    }
}
