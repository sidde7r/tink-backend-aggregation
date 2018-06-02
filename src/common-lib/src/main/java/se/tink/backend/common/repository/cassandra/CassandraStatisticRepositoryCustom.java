package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.concurrent.ExecutionException;
import se.tink.backend.core.CassandraStatistic;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.libraries.date.ResolutionTypes;

public interface CassandraStatisticRepositoryCustom extends Creatable {
    List<CassandraStatistic> findStatistics(String userId, List<ResolutionTypes> resolutions);

    List<CassandraStatistic> findStatistics(String userId, List<ResolutionTypes> resolutions, List<Integer> periodHeads);

    @SuppressWarnings("unused") // will be used in the future for querying
    List<CassandraStatistic> findStatistics(String userId, int periodHead,
            String resolution);

    <S extends CassandraStatistic> void saveByQuorum(Iterable<S> batch);

    <S extends CassandraStatistic> S saveByQuorum(S entity);

    void deleteByUserIdAndResolution(String userId, ResolutionTypes resolution);
}
