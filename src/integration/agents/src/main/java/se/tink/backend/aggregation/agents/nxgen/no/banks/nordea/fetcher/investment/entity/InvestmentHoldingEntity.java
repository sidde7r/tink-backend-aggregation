package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class InvestmentHoldingEntity {

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
}
