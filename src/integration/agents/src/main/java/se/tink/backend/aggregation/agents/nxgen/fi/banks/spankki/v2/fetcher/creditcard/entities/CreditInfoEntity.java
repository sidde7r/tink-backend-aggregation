package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditInfoEntity {
    @JsonProperty private String creditName;
    @JsonProperty private int dueDDateInMonth;
    @JsonProperty private BigDecimal currentPeriodAmount;
    @JsonProperty private BigDecimal minPaymentPercent;
    @JsonProperty private String dueDate;
}
