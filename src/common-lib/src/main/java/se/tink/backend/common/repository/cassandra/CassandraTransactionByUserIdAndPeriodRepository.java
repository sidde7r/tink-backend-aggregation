package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CassandraTransactionByUserIdPeriod;

public interface CassandraTransactionByUserIdAndPeriodRepository
        extends CassandraTransactionByUserIdAndPeriodRepositoryCustom,
        CassandraRepository<CassandraTransactionByUserIdPeriod> {
    <S extends CassandraTransactionByUserIdPeriod> S save(S s);

    <S extends CassandraTransactionByUserIdPeriod> Iterable<S> save(Iterable<S> iterable);

    @Override
    void deleteAll();
}
