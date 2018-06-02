package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionProcessorConfiguration {

    @JsonProperty
    private int monthsForProcessing = 12;

    @JsonProperty
    private boolean statisticsActivitiesEnabled = true;

    public int getMonthsForProcessing() {
        return monthsForProcessing;
    }

    public boolean isStatisticsActivitiesEnabled() {
        return statisticsActivitiesEnabled;
    }

}
