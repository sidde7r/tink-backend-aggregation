package se.tink.backend.aggregation.storage.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;

public interface ClientConfigurationsRepository extends JpaRepository<ClientConfiguration, String> {
}
