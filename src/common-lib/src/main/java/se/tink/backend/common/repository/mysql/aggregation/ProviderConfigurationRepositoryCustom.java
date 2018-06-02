package se.tink.backend.common.repository.mysql.aggregation;

import java.util.List;
import se.tink.backend.core.ProviderConfiguration;

public interface ProviderConfigurationRepositoryCustom {
    ProviderConfiguration findByClusterIdAndProviderName(String clusterId, String providerName);
    List<ProviderConfiguration> findAllByClusterIdAndMarket(String clusterId, String market);
    List<ProviderConfiguration> findAllByClusterId(String clusterId);
    List<ProviderConfiguration> findAllByMarket(String market);
    ProviderConfiguration findByName(String name);
}
