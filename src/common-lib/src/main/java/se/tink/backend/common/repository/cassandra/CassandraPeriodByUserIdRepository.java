package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CassandraPeriodByUserId;

public interface CassandraPeriodByUserIdRepository
        extends CassandraPeriodByUserIdRepositoryCustom,
        CassandraRepository<CassandraPeriodByUserId> {
    <S extends CassandraPeriodByUserId> S save(S s);

    <S extends CassandraPeriodByUserId> Iterable<S> save(Iterable<S> iterable);
}
