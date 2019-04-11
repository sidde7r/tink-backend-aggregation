package se.tink.backend.aggregation.provider.configuration.storage;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationByCluster;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;

public class ClusterProviderHandler {

    private final Map<String, ProviderConfigurationByCluster> providerConfigurationByClusterMap;

    // Iterate over the clusters and create an object that holds the "final" provider configuration.
    // TODO: instead of injecting in-memory objects, change to a object that holds nothing in
    // memory, but makes the data available
    @Inject
    ClusterProviderHandler(
            @Named("providerConfiguration")
                    Map<String, ProviderConfigurationStorage> providerConfigurationByName,
            @Named("enabledProvidersOnCluster") Map<String, Set<String>> enabledProvidersOnCluster,
            @Named("providerOverrideOnCluster")
                    Map<String, Map<String, ProviderConfigurationStorage>>
                            providerOverrideOnCluster,
            @Named("capabilitiesByAgent")
                    Map<String, Set<ProviderConfigurationStorage.Capability>>
                            capabilitiesByAgentClass) {

        this.providerConfigurationByClusterMap =
                generateProviderConfigurationByClusterMap(
                        providerConfigurationByName,
                        enabledProvidersOnCluster,
                        providerOverrideOnCluster,
                        capabilitiesByAgentClass);
    }

    private static Map<String, ProviderConfigurationByCluster>
            generateProviderConfigurationByClusterMap(
                    Map<String, ProviderConfigurationStorage> providerConfigurationByName,
                    Map<String, Set<String>> enabledProvidersOnCluster,
                    Map<String, Map<String, ProviderConfigurationStorage>>
                            providerOverrideOnCluster,
                    Map<String, Set<ProviderConfigurationStorage.Capability>>
                            capabilitiesByAgentClass) {

        Map<String, ProviderConfigurationByCluster> providerConfigurationByClusterMap =
                Maps.newHashMap();
        for (String clusterId : enabledProvidersOnCluster.keySet()) {
            providerConfigurationByClusterMap.put(
                    clusterId,
                    new ProviderConfigurationByCluster(
                            clusterId,
                            enabledProvidersOnCluster.get(clusterId),
                            providerOverrideOnCluster.getOrDefault(
                                    clusterId, Collections.emptyMap()),
                            providerConfigurationByName,
                            capabilitiesByAgentClass));
        }

        return providerConfigurationByClusterMap;
    }

    public boolean validate(String clusterId) {
        return providerConfigurationByClusterMap.containsKey(clusterId);
    }

    public boolean validate(String clusterId, String market) {
        return validate(clusterId)
                && providerConfigurationByClusterMap.get(clusterId).marketEnabled(market);
    }

    public Map<String, ProviderConfigurationStorage> getProviderConfigurationForCluster(
            String clusterId) {
        return providerConfigurationByClusterMap.get(clusterId).getProviderConfigurations();
    }

    public ProviderConfigurationStorage getProviderConfiguration(
            String clusterId, String providerName) {
        ProviderConfigurationByCluster providerConfigurationByCluster =
                providerConfigurationByClusterMap.get(clusterId);
        if (Objects.isNull(providerConfigurationByCluster)) {
            return null;
        }

        return providerConfigurationByCluster.getProviderConfiguration(providerName);
    }
}
