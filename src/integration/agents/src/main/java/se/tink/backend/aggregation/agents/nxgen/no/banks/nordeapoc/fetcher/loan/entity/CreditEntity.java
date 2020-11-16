package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.entity;

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
