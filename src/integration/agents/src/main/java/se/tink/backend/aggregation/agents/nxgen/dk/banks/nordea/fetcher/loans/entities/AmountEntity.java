package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AmountEntity {

    private BigDecimal granted;
    private BigDecimal paid;
    private BigDecimal balance;
}
