package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RefundSummaryEntity {
    @JsonProperty("AccountBalance")
    private int accountBalance;
    @JsonProperty("AccountType")
    private int accountType;
    @JsonProperty("MonthName")
    private String monthName;
    @JsonProperty("PeriodRefund")
    private int periodRefund;
    @JsonProperty("ProfileNextRateDistance")
    private int profileNextRateDistance;
    @JsonProperty("ProfileRate")
    private int profileRate;
    @JsonProperty("TotalRefund")
    private int totalRefund;
}
