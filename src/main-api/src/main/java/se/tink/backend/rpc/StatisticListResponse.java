package se.tink.backend.rpc;

import io.protostuff.Tag;
import se.tink.backend.core.Statistic;

import java.util.List;

public class StatisticListResponse {
    @Tag(1)
    private List<Statistic> statistics;

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<Statistic> statistics) {
        this.statistics = statistics;
    }
}
