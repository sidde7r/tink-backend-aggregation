package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;

public class StatisticConfiguration {
    /**
     * Calculate yearly statistic on demand when queried for.
     */
    @JsonProperty
    public boolean provideYearly = false;

    @JsonProperty
    public int monthsOfStatistics = 12;

    @VisibleForTesting
    public StatisticConfiguration(boolean provideYearly) {
        this.provideYearly = provideYearly;
    }

    @JsonCreator
    StatisticConfiguration() {
    }

    public boolean provideYearly() {
        return provideYearly;
    }

    public int getMonthsOfStatistics() { return monthsOfStatistics; }
}
