package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.UserLocation;

public interface UserLocationRepository extends CassandraRepository<UserLocation>, UserLocationRepositoryCustom {

}
