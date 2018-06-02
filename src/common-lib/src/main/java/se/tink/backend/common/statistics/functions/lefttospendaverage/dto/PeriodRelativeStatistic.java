package se.tink.backend.common.statistics.functions.lefttospendaverage.dto;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import javax.validation.constraints.NotNull;
import org.joda.time.DateTime;
import se.tink.backend.core.Statistic;

public class PeriodRelativeStatistic implements Comparable<PeriodRelativeStatistic> {
    private final DateStatistic dateStatistic;
    private final double periodRelativePercentage;

    public PeriodRelativeStatistic(Range<DateTime> periodRange, DateStatistic dateStatistic) {
        this.dateStatistic = dateStatistic;
        this.periodRelativePercentage = calculatePeriodRelativePercentage(periodRange, dateStatistic);
    }

    private double calculatePeriodRelativePercentage(Range<DateTime> periodRange, DateStatistic statistic) {
        double timeFromStart = statistic.getDateTime().getMillis() - periodRange.lowerEndpoint().getMillis();
        double periodTotalTime = periodRange.upperEndpoint().getMillis() - periodRange.lowerEndpoint().getMillis();

        double percentage = timeFromStart / periodTotalTime;

        Preconditions.checkState(percentage <= 1.0, "Date of statistic is more than maximum: " + percentage);
        Preconditions.checkState(percentage >= 0.0, "Date of statistic is less than minimum: " + percentage);

        return percentage;
    }

    public double getPeriodRelativePercentage() {
        return periodRelativePercentage;
    }

    public DateTime getLocalDateTime() {
        return dateStatistic.getDateTime();
    }

    public Statistic getStatistic() {
        return dateStatistic.getStatistic();
    }

    public DateStatistic getDateStatistic() {
        return dateStatistic;
    }

    @Override
    public int compareTo(@NotNull PeriodRelativeStatistic other) {
        return Double.compare(periodRelativePercentage, other.periodRelativePercentage);
    }
}
