package se.tink.backend.common.repository.mysql.aggregation.clientconfigurations;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClientConfigurations;

public interface ClientConfigurationsRepository extends JpaRepository<ClientConfigurations, String> {
}
