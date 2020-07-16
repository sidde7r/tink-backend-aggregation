package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class Balance {
    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("value")
    private BigDecimal value;
}
