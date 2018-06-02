package se.tink.backend.system.statistics;

import com.google.common.base.Objects;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.CassandraStatistic;
import se.tink.backend.core.MinimizedStatistic;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticContainer;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.uuid.UUIDUtils;

public class StatisticsTransformer {
    public static List<CassandraStatistic> transform(final StatisticContainer statisticsContainer) {
        final List<Statistic> statistics = statisticsContainer.getStatistics();
        final String userId = statisticsContainer.getUserId();
        return statistics.stream().collect(Collectors.groupingBy(StatisticsGroup::new))
                .entrySet()
                .stream()
                .map(group -> {
                    final StatisticsGroup groupKey = group.getKey();
                    final List<Statistic> groupKeyStatistics = group.getValue();

                    CassandraStatistic cassandraStatistics = new CassandraStatistic();
                    cassandraStatistics.setPeriodHead(groupKey.truncatedPeriod);
                    cassandraStatistics.setUserId(UUIDUtils.fromTinkUUID(userId));
                    cassandraStatistics.setResolution(groupKey.resolution);
                    cassandraStatistics.setType(groupKey.type);

                    List<MinimizedStatistic> minimizedStatistics = groupKeyStatistics.stream().map(s -> {
                        MinimizedStatistic minimizedStatistic = new MinimizedStatistic();
                        minimizedStatistic.setValue(s.getValue());
                        minimizedStatistic.setPeriod(s.getPeriod());
                        minimizedStatistic.setDescription(s.getDescription());
                        return minimizedStatistic;
                    }).collect(Collectors.toList());

                    cassandraStatistics.setStatistics(minimizedStatistics);

                    return cassandraStatistics;
                })
                .collect(Collectors.toList());
    }
}

class StatisticsGroup {
    public final String userId;
    public final Integer truncatedPeriod;
    public final String type;
    public final ResolutionTypes resolution;

    public StatisticsGroup(Statistic statistic) {
        this.userId = statistic.getUserId();
        this.truncatedPeriod = statistic.getTruncatedPeriod();
        this.type = statistic.getType();
        this.resolution = statistic.getResolution();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StatisticsGroup that = (StatisticsGroup) o;
        return Objects.equal(userId, that.userId) &&
                Objects.equal(truncatedPeriod, that.truncatedPeriod) &&
                Objects.equal(type, that.type) &&
                resolution == that.resolution;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, truncatedPeriod, type, resolution);
    }
}
