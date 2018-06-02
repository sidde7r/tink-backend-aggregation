package se.tink.backend.common.dao;

import com.google.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.core.CassandraStatistic;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticContainer;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.serialization.proto.utils.ProtoSerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class StatisticDao {
    private static final List<ResolutionTypes> RESOLUTION_TYPES = Arrays.stream(ResolutionTypes.values())
            .filter(rt -> !rt.equals(ResolutionTypes.YEARLY)).collect(Collectors.toList());

    private static final int CACHE_EXPIRY_SECONDS = (int) Duration.ofDays(7).getSeconds();
    private static final Logger log = LoggerFactory.getLogger(StatisticDao.class);
    private final CassandraStatisticRepository cassandraStatisticRepository;
    private final CacheClient cacheClient;
    private final Timer cassandraWriteTimer;
    private final Timer cassandraCacheWriteTimer;
    private final Timer cassandraCacheDeleteTimer;

    @Inject
    public StatisticDao(CassandraStatisticRepository statsRepo,
            CacheClient cacheClient, MetricRegistry metricRegistry) {
        this.cassandraStatisticRepository = statsRepo;
        this.cacheClient = cacheClient;

        MetricId statisticWriteMetricId = MetricId.newId("statistics_storage");
        cassandraWriteTimer = metricRegistry.timer(statisticWriteMetricId.label("type", "cassandra_write"));
        cassandraCacheWriteTimer = metricRegistry.timer(statisticWriteMetricId
                .label("type", "cassandra_cache"));
        cassandraCacheDeleteTimer = metricRegistry
                .timer(statisticWriteMetricId.label("type", "cassandra_delete_cache"));
    }

    public List<Statistic> findByUserId(String userId) {
        return findCassandraStatistics(userId);
    }

    public List<Statistic> findAllByUserIdAndPeriods(String userId, List<Integer> periods) {
        return findCassandraStatisticsByUserIdAndPeriods(userId, periods);
    }

    public void deleteByUserId(String userId) {
        // Always try to delete from Cassandra, because user is deleted before its statistics, we don't have access
        // to feature flags anymore
        Arrays.stream(ResolutionTypes.values()).forEach(rt ->
                cassandraStatisticRepository.deleteByUserIdAndResolution(userId, rt)
        );
        cacheClient.delete(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId);

        cacheClient.delete(CacheScope.STATISTICS_BY_USERID, userId);
    }

    /**
     * A temporary method to work around how ProcessServiceResource.generateStatisticsAndActivities is implemented
     * (expects caching and saving to database to be two separate actions). CassandraStatistic should not be exposed
     * outside of DAO.
     */
    @Deprecated
    public void cache(List<CassandraStatistic> cassandraStatistics,
            StatisticContainer statisticContainer) {
        Timer.Context cassandraTiming = cassandraCacheWriteTimer.time();
        cacheCassandraStatistics(cassandraStatistics, statisticContainer.getUserId());
        cassandraTiming.stop();
    }

    public void invalidateCache(String userId) {
        Timer.Context cassandraTiming = cassandraCacheDeleteTimer.time();
        cacheClient.delete(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId);
        cassandraTiming.stop();
    }

    /**
     * @see #cache(List, StatisticContainer)
     */
    @Deprecated
    public void save(List<String> featureFlags,
            List<CassandraStatistic> cassandraStatistics, StatisticContainer statisticContainer) {
        Timer.Context cassandraTiming = cassandraWriteTimer.time();
        saveBatch(cassandraStatistics);
        cassandraTiming.stop();
    }

    public <S extends CassandraStatistic> void saveBatch(Iterable<S> batch) {
        cassandraStatisticRepository.saveByQuorum(batch);
    }

    public <S extends CassandraStatistic> S save(S entity) {
        return cassandraStatisticRepository.saveByQuorum(entity);
    }

    private List<CassandraStatistic> deserializeStatistics(byte[] statsBytes) {
        List<CassandraStatistic> cassandraStatistics = null;

        if (statsBytes != null) {
            try {
                cassandraStatistics = ProtoSerializationUtils
                        .deserializeFromBinary(Snappy.uncompress(statsBytes), CassandraStatistic.class);
            } catch (IOException e) {
                log.error("Couldn't uncompress/read statistics data ", e);
                // We couldn't deserialize the cache, query from database instead
                return null;
            }
        }

        return cassandraStatistics;
    }

    private List<Statistic> findCassandraStatistics(String userId) {
        byte[] statsBytes = (byte[]) cacheClient.get(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId);
        List<CassandraStatistic> cassandraStatistics = deserializeStatistics(statsBytes);

        if (cassandraStatistics == null) {
            // Cache miss.
            cassandraStatistics = cassandraStatisticRepository
                    .findStatistics(userId, RESOLUTION_TYPES);
        }

        if (!cassandraStatistics.isEmpty()) {
            cacheCassandraStatistics(cassandraStatistics, userId);
        }

        return cassandraStatistics.stream().flatMap(cassandraStatisticsStreamFunction)
                .collect(Collectors.toList());
    }

    private List<Statistic> findCassandraStatisticsByUserIdAndPeriods(String userId, List<Integer> periods) {
        // When we no longer re-generate statistics for all months, this will need to be per month.
        byte[] statsBytes = (byte[]) cacheClient.get(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId);
        List<CassandraStatistic> cassandraStatistics = deserializeStatistics(statsBytes);

        if (cassandraStatistics == null) {
            // Cache miss.
            cassandraStatistics = cassandraStatisticRepository
                    .findStatistics(userId, RESOLUTION_TYPES, periods);
        }

        if (!cassandraStatistics.isEmpty()) {
            cacheCassandraStatistics(cassandraStatistics, userId);
        }

        List<Statistic> statistics = cassandraStatistics.stream().flatMap(cassandraStatisticsStreamFunction)
                .collect(Collectors.toList());

        return statistics;
    }

    private void cacheCassandraStatistics(List<CassandraStatistic> cassandraStatistics, String userId) {
        try {
            byte[] compressed = Snappy.compress(
                    ProtoSerializationUtils.serializeToBinary(cassandraStatistics, CassandraStatistic.class));
            cacheClient.set(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId, CACHE_EXPIRY_SECONDS,
                    compressed);
        } catch (IOException e) {
            throw new CacheWriteException(e);
        }
    }

    public void createTableIfNotExist() {
        cassandraStatisticRepository.createTableIfNotExist();
    }

    private Function<CassandraStatistic, Stream<Statistic>> cassandraStatisticsStreamFunction = cs -> cs
            .getMinimizedStatistics().stream().map(ms -> {
                Statistic statistic = new Statistic();
                statistic.setUserId(UUIDUtils.toTinkUUID(cs.getUserId()));
                statistic.setDescription(ms.getDescription());
                statistic.setPeriod(ms.getPeriod());
                statistic.setType(cs.getType());
                statistic.setResolution(cs.getResolution());
                statistic.setValue(ms.getValue());
                return statistic;
            });

}
