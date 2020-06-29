package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class HandelsbankenSEPensionValue {

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("amountFormatted")
    private String amountFormatted;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAmountFormatted() {
        return amountFormatted;
    }
}
