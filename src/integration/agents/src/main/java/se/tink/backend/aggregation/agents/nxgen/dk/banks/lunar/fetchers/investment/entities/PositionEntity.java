package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PositionEntity {
    private BigDecimal amount;
    private Double averagePriceInBaseCurrency;
    private Double profitLossOnTradeInBaseCurrency;
}
