package se.tink.backend.common.workers.activity.generators.models;

import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.backend.core.Statistic;

public class LeftToSpendActivityData {
    private Double difference;
    private List<Statistic> leftToSpend;
    private List<Statistic> leftToSpendAverage;

    public Double getDifference() {
        return difference;
    }

    public void setDifference(Double difference) {
        this.difference = difference;
    }

    public List<Statistic> getLeftToSpendAverage() {
        return leftToSpendAverage;
    }

    public void setLeftToSpendAverage(List<Statistic> leftToSpendAverage) {
        this.leftToSpendAverage = leftToSpendAverage;
    }

    public List<Statistic> getLeftToSpend() {
        return leftToSpend;
    }

    public void setLeftToSpend(List<Statistic> leftToSpend) {
        this.leftToSpend = leftToSpend;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("difference", difference).add("leftToSpend", getLeftToSpend())
                .add("leftToSpendAverage", getLeftToSpendAverage()).toString();
    }
}
