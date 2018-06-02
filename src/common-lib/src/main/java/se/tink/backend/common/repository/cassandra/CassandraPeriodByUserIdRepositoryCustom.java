package se.tink.backend.common.repository.cassandra;

import java.util.List;
import se.tink.backend.common.health.Checkable;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface CassandraPeriodByUserIdRepositoryCustom extends Creatable, Checkable {
    void deleteByUserId(String userId);

    void deleteByUserIdAndPeriod(String userId, int period);

    List<CassandraPeriodByUserId> findByUserId(String userId);

    List<CassandraPeriodByUserId> findByUserIdAndPeriod(String userId, int period);

    <S extends CassandraPeriodByUserId> S saveByQuorum(S entity);

    <S extends CassandraPeriodByUserId> Iterable<S> saveByQuorum(Iterable<S> entities, int batchSize);

    void deleteByQuorum(Iterable<? extends CassandraPeriodByUserId> entities, int batchSize);
}
