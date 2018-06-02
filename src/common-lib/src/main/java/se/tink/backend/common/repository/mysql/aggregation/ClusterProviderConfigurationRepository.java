package se.tink.backend.common.repository.mysql.aggregation;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterProviderConfiguration;
import se.tink.backend.core.ClusterProviderId;

public interface ClusterProviderConfigurationRepository extends JpaRepository<ClusterProviderConfiguration, ClusterProviderId> {
}
