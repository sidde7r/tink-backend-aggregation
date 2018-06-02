package se.tink.backend.consent.repository.cassandra;

import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.consent.core.cassandra.CassandraUserConsent;

public interface UserConsentRepository extends CassandraRepository<CassandraUserConsent>, UserConsentRepositoryCustom {
    List<CassandraUserConsent> findAllByUserId(UUID userId);
    CassandraUserConsent findByUserIdAndId(UUID userId, UUID id);
}
