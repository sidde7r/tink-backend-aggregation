package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class InvestmentEntity {

    private String accountNumber;
    private double cashAmount;
    private String currency;
    private String id;
    private double marketValue;
    private String name;

    @JsonProperty("profit_loss")
    private double profit;

    @JsonProperty("profit_loss_percentage")
    private double profitPercentage;

    private List<InvestmentHoldingEntity> holdings;
}
