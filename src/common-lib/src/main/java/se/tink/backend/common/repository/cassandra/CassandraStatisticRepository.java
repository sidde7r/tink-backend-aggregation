package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CassandraStatistic;

public interface CassandraStatisticRepository extends CassandraStatisticRepositoryCustom, CassandraRepository<CassandraStatistic>{
}
