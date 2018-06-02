package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.OAuth2ClientEvent;

public interface OAuth2ClientEventRepository extends CassandraRepository<OAuth2ClientEvent>, OAuth2ClientEventRepositoryCustom {
    
}
