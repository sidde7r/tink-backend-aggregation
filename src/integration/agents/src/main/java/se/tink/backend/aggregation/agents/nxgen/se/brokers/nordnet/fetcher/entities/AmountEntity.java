package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AmountEntity {

    private String currency;
    private BigDecimal value;

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getValue() {
        return value;
    }
}
