package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreditEntity {
    private BigDecimal limit;
    private BigDecimal available;
    private BigDecimal spent;
}
