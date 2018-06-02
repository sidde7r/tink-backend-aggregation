package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.BankFeeStatistics;

public interface BankFeeStatisticsRepository
        extends CassandraRepository<BankFeeStatistics>, BankFeeStatisticsRepositoryCustom {
}