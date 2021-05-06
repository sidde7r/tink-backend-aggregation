package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AvailableBalanceEntity {
    private BigDecimal amount;

    private String currency;
}
