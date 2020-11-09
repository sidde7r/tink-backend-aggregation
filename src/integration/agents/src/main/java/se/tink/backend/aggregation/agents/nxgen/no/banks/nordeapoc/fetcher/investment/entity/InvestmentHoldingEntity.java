package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class InvestmentHoldingEntity {

    private String id;
    private String name;
    private String currency;

    private InstrumentEntity instrument;

    private double quantity;
    private double avgPurchasePrice;
    private double marketValue;

    @JsonProperty("profit_loss")
    private double profit;
}
