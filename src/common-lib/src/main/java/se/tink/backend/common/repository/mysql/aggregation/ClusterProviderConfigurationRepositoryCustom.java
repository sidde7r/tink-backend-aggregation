package se.tink.backend.common.repository.mysql.aggregation;

import java.util.List;
import se.tink.backend.core.ClusterProviderConfiguration;

public interface ClusterProviderConfigurationRepositoryCustom {
    List<ClusterProviderConfiguration> findAllByClusterProviderIdClusterId(String clusterId);
}
