package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.UserCoordinates;

public interface UserCoordinatesRepository extends CassandraRepository<UserCoordinates>, UserCoordinatesRepositoryCustom {

}
