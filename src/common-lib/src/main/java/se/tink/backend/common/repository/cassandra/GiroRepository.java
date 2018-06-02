package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.giros.Giro;

public interface GiroRepository extends CassandraRepository<Giro>, GiroRepositoryCustom {

}
