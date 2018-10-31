package se.tink.backend.aggregation.configurations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.configurations.models.ClientConfiguration;

public interface ClientConfigurationsRepository extends JpaRepository<ClientConfiguration, String> {
}
