package se.tink.backend.aggregation.provider.configuration.storage.models;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Holds the final Provider Configuration data for the specific clusterId.
// Upon instantiation the object determines which configuration to choose from
// the enabled/global/override data and stores them in memory
public class ProviderConfigurationByCluster {
    private static Logger logger = LoggerFactory.getLogger(ProviderConfigurationByCluster.class);
    private final String clusterId;
    private Set<String> enabledMarkets;

    // provider name, provider configuration
    private Map<String, ProviderConfigurationStorage> providerConfigurations;

    // We do these procumptations once because they are expensive.
    // For all enabled providers
    // Creates a final list of all the enabled provider configuration, combining overrides or not.
    public ProviderConfigurationByCluster(
            String clusterId,
            Set<String> enabledProviders,
            Map<String, ProviderConfigurationStorage> providerConfigurationOverrides,
            Map<String, ProviderConfigurationStorage> allProviderConfiguration,
            Map<String, Set<ProviderConfigurationStorage.Capability>> capabilitiesByAgentClass) {

        this.providerConfigurations = selectProviderConfigurations(clusterId, enabledProviders,
                providerConfigurationOverrides, allProviderConfiguration, capabilitiesByAgentClass);
        this.clusterId = clusterId;
        this.enabledMarkets = getEnabledMarkets(providerConfigurations.values());
    }

    private static Map<String, ProviderConfigurationStorage> selectProviderConfigurations(String clusterId,
            Set<String> enabledProviders,
            Map<String, ProviderConfigurationStorage> providerConfigurationOverrides,
            Map<String, ProviderConfigurationStorage> allProviderConfiguration,
            Map<String, Set<ProviderConfigurationStorage.Capability>> capabilitiesByAgentClass) {

        Map<String, ProviderConfigurationStorage> providerConfigurations = Maps.newHashMap();
        enabledProviders.forEach(
                providerName -> {
                    ProviderConfigurationStorage providerConfigurationStorage = getProviderConfiguration(
                            providerName, providerConfigurationOverrides, allProviderConfiguration);

                    if (providerConfigurationStorage == null) {
                        logger.error("Could not find configuration for enabled provider {} and cluster {}",
                                providerName, clusterId);
                        return;
                    }

                    // Get capabilities from agent instead of provider configuration
                    providerConfigurationStorage.setCapabilities(
                            capabilitiesByAgentClass.getOrDefault(
                                    providerConfigurationStorage.getClassName(),
                                    Collections.emptySet()));

                    providerConfigurations.put(providerName, providerConfigurationStorage);
                }
        );

        return providerConfigurations;
    }

    private static ProviderConfigurationStorage getProviderConfiguration(String providerName,
            Map<String, ProviderConfigurationStorage> providerConfigurationOverrides,
            Map<String, ProviderConfigurationStorage> allProviderConfiguration) {

        if (providerConfigurationOverrides.containsKey(providerName)) {
            return providerConfigurationOverrides.get(providerName);
        }

        return allProviderConfiguration.get(providerName);
    }

    private static Set<String> getEnabledMarkets(Collection<ProviderConfigurationStorage> providerConfigurationStorages) {
        return providerConfigurationStorages.stream()
                .map(ProviderConfigurationStorage::getMarket)
                .collect(Collectors.toSet());
    }

    public boolean marketEnabled(String market) {
        return this.enabledMarkets.contains(market);
    }

    // return a new map, so the original remains unaltered, in case of any alterations.
    public Map<String, ProviderConfigurationStorage> getProviderConfigurations() {
        return Maps.newHashMap(this.providerConfigurations);
    }

    public ProviderConfigurationStorage getProviderConfiguration(String providerName) {
        return providerConfigurations.get(providerName);
    }
}
