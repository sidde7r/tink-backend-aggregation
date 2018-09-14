package se.tink.backend.aggregation.provider.configuration.repositories.mysql;

import se.tink.backend.aggregation.provider.configuration.repositories.ProviderConfiguration;

import java.util.List;

public interface ProviderConfigurationRepositoryCustom {
    ProviderConfiguration findByClusterIdAndProviderName(String clusterId, String providerName);
    List<ProviderConfiguration> findAllByClusterIdAndMarket(String clusterId, String market);
    List<ProviderConfiguration> findAllByClusterId(String clusterId);
    List<ProviderConfiguration> findAllByMarket(String market);
    ProviderConfiguration findByName(String name);
}
