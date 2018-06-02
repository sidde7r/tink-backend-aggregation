package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CredentialsEvent;

public interface CredentialsEventRepository extends CassandraRepository<CredentialsEvent>, CredentialsEventRepositoryCustom {
    
}
