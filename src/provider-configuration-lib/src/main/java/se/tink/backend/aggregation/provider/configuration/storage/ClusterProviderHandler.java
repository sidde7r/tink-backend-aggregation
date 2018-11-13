package se.tink.backend.aggregation.provider.configuration.storage;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationByCluster;

public class ClusterProviderHandler {

    // clusterId, all providerConfiguration for that clusterId
    private final Map<String, ProviderConfigurationByCluster> providerConfigurationByClusterMap;

    @Inject
    ClusterProviderHandler(
            @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName,
            @Named("enabledProvidersOnCluster") Map<String, Set<String>> enabledProvidersOnCluster,
            @Named("providerOverrideOnCluster") Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster) {
        this.providerConfigurationByClusterMap = createProviderConfigurationByClusterMap(providerConfigurationByName,
                enabledProvidersOnCluster, providerOverrideOnCluster);
    }

    // iterate over the clusters and create an object that holds the "final" provider configuration.
    private Map createProviderConfigurationByClusterMap(
            Map<String, ProviderConfiguration> providerConfigurationByName,
            Map<String, Set<String>> enabledProvidersOnCluster,
            Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster) {
        Map<String, ProviderConfigurationByCluster> providerConfigurationByCluster = Maps.newHashMap();
        for (String clusterId : enabledProvidersOnCluster.keySet()) {
            providerConfigurationByCluster.put(clusterId,
                    new ProviderConfigurationByCluster(
                            clusterId,
                            enabledProvidersOnCluster.get(clusterId),
                            providerOverrideOnCluster.getOrDefault(clusterId, Collections.emptyMap()),
                            providerConfigurationByName));
        }
        return providerConfigurationByCluster;
    }

    public boolean validate(String clusterId) {
        return providerConfigurationByClusterMap.containsKey(clusterId);
    }

    public boolean validate(String clusterId, String market) {
        return validate(clusterId) && providerConfigurationByClusterMap.get(clusterId).marketEnabled(market);
    }

    public Map<String, ProviderConfiguration> getProviderConfigurationForCluster(String clusterId) {
        return providerConfigurationByClusterMap.get(clusterId).getProviderConfigurations();
    }

    public ProviderConfiguration getProviderConfiguration(String clusterId, String providerName) {
        ProviderConfigurationByCluster providerConfigurationByCluster = providerConfigurationByClusterMap.get(clusterId);
        if (Objects.isNull(providerConfigurationByCluster)) {
            return null;
        }

        return providerConfigurationByCluster.getProviderConfiguration(providerName);
    }
}
