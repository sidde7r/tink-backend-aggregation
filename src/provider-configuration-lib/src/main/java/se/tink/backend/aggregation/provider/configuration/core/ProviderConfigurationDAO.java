package se.tink.backend.aggregation.provider.configuration.core;

import java.util.List;

public interface ProviderConfigurationDAO {
    List<ProviderConfiguration> findAll();

    List<ProviderConfiguration> findAllByClusterId(String id);

    List<ProviderConfiguration> findAllByClusterIdAndMarket(String id, String market);

    List<ProviderConfiguration> findAllByMarket(String market);

    ProviderConfiguration findByClusterIdAndProviderName(String id, String providerName);

    ProviderConfiguration findByName(String providerName);

    void saveAndFlush(ProviderConfiguration provider);
}
