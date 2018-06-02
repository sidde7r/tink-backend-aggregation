package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.CassandraUserDemographics;

public interface CassandraUserDemographicsRepository extends CassandraRepository<CassandraUserDemographics>,
        CassandraUserDemographicsRepositoryCustom {

}
