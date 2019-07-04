package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SummaryEntity {
    @JsonProperty("DateOfLastTrade")
    private String dateOfLastTrade;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("StatusText")
    private String statusText;

    @JsonProperty("TotalHoldingAquisitionValue")
    private double totalHoldingAcquisitionValue;

    @JsonProperty("TotalHoldingMarketValue")
    private double totalHoldingMarketValue;

    @JsonProperty("TotalHoldingPerformance")
    private double totalHoldingPerformance;

    @JsonProperty("TotalHoldingPerformanceSEK")
    private double totalHoldingPerformancesek;

    @JsonProperty("TotalPremiumPayment")
    private double totalPremiumPayment;

    @JsonProperty("TradeStatus")
    private String tradeStatus;

    @JsonProperty("TradeStatusText")
    private String tradeStatusText;
}
