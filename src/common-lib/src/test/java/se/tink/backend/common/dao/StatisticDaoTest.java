package se.tink.backend.common.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.elasticsearch.index.engine.IgnoreOnRecoveryEngineException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.xerial.snappy.Snappy;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.common.repository.mysql.main.StatisticRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.CassandraStatistic;
import se.tink.backend.core.MinimizedStatistic;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticContainer;
import se.tink.backend.core.User;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.proto.utils.ProtoSerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatisticDaoTest {
    private static final Random random = new Random();

    @Mock CacheClient cacheClient;
    @Mock CassandraStatisticRepository cassandraStatisticRepository;
    @Mock UserRepository userRepository;
    @SuppressWarnings("unused") @Mock MetricRegistry metricRegistry;
    @InjectMocks StatisticDao statisticDao;

    private CassandraStatistic createTestStatistic() {
        MinimizedStatistic minimized = new MinimizedStatistic();
        minimized.setDescription("test");
        minimized.setPeriod("monthly");
        minimized.setValue(random.nextDouble());

        CassandraStatistic cs = new CassandraStatistic(Collections.singletonList(minimized));
        cs.setResolution(ResolutionTypes.MONTHLY);
        cs.setPeriodHead(random.nextInt());
        cs.setType("monthly");
        return cs;
    }

    @Test
    public void deleteUserStatistics() {
        assertNull(userRepository.findOne("userId"));

        statisticDao.deleteByUserId("userId");

        verify(cacheClient).delete(CacheScope.CASSANDRA_STATISTICS_BY_USERID, "userId");
        for (ResolutionTypes rt : ResolutionTypes.values()) {
            verify(cassandraStatisticRepository).deleteByUserIdAndResolution("userId", rt);
        }
    }

    @Test
    public void findByUserIdInCache() throws IOException {
        final String userId = "userId";
        CassandraStatistic cs = createTestStatistic();

        byte[] compressed = Snappy.compress(
                ProtoSerializationUtils.serializeToBinary(Arrays.asList(cs), CassandraStatistic.class));

        when(cacheClient.get(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId))
                .thenReturn(new StatisticContainer(compressed).getData());

        List<Statistic> statistics = statisticDao.findByUserId("userId");
        assertEquals(statistics.size(), 1);

        Statistic s = statistics.get(0);
        assertEquals(cs.getResolution(), s.getResolution());
        assertEquals(cs.getType(), s.getType());
        assertEquals(cs.getUserId(), s.getUserId());
    }

    @Test
    public void findByUserIdInDatabase() throws IOException {
        final String userId = "userId";
        CassandraStatistic cs = createTestStatistic();

        when(cacheClient.get(CacheScope.CASSANDRA_STATISTICS_BY_USERID, userId))
                .thenReturn(null);

        when(cassandraStatisticRepository.findStatistics(eq("userId"), anyList())).thenReturn(Arrays.asList(cs));

        List<Statistic> statistics = statisticDao.findByUserId("userId");
        assertEquals(statistics.size(), 1);

        Statistic s = statistics.get(0);
        assertEquals(cs.getResolution(), s.getResolution());
        assertEquals(cs.getType(), s.getType());
        assertEquals(cs.getUserId(), s.getUserId());
    }
}
