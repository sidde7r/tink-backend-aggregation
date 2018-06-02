package se.tink.backend.consent.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.consent.core.cassandra.CassandraConsent;

public interface ConsentRepository extends CassandraRepository<CassandraConsent>, ConsentRepositoryCustom {

}
