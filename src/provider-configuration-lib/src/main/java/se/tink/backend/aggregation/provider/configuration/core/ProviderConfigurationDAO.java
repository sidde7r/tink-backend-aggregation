package se.tink.backend.aggregation.provider.configuration.core;

import se.tink.backend.core.ProviderStatuses;

import java.util.List;

public interface ProviderConfigurationDAO {
    List<ProviderConfiguration> findAllByClusterId(String id);

    List<ProviderConfiguration> findAllByClusterIdAndMarket(String id, String market);

    ProviderConfiguration findByClusterIdAndProviderName(String id, String providerName);

    void updateStatus(String providerName, ProviderStatuses providerStatus);

    void removeStatus(String providerName);
}
