package se.tink.backend.aggregation.provider.configuration.storage.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Holds the final Provider Configuration data for the specific clusterId.
// Upon instantiation the object determines which configuration to choose from
// the enabled/global/override data and stores them in memory
public class ProviderConfigurationByCluster {

    private Logger logger = LoggerFactory.getLogger(ProviderConfigurationByCluster.class);
    private final String clusterId;
    private Set<String> enabledMarkets;

    // provider name, provider configuration
    private Map<String, ProviderConfiguration> providerConfigurations;

    // We do these procumptations once because they are expensive.
    // For all enabled providers
    // Creates a final list of all the enabled provider configuration, combining overrides or not.
    public ProviderConfigurationByCluster(
            String clusterId,
            Set<String> enabledProviders,
            Map<String, ProviderConfiguration> providerConfigurationOverrides,
            Map<String, ProviderConfiguration> allProviderConfiguration,
            Map<String, Set<ProviderConfiguration.Capability>> capabilitiesByAgentClass) {
        this.providerConfigurations = Maps.newHashMap();
        this.enabledMarkets = Sets.newHashSet();
        this.clusterId = clusterId;
        enabledProviders.forEach(
                providerName -> {
                    ProviderConfiguration providerConfiguration;
                    if (providerConfigurationOverrides.containsKey(providerName)) {
                        providerConfiguration = providerConfigurationOverrides.get(providerName);
                    } else if (allProviderConfiguration.containsKey(providerName)) {
                        providerConfiguration = allProviderConfiguration.get(providerName);
                    } else {
                        logger.error("Could not find configuration for enabled provider {} and cluster {}",
                                providerName, clusterId);
                        return;
                    }

                    // Get capabilities from agent instead of provider configuration
                    providerConfiguration.setCapabilities(
                            capabilitiesByAgentClass.getOrDefault(
                                    providerConfiguration.getClassName(),
                                    Collections.emptySet()));

                    providerConfigurations.put(providerName, providerConfiguration);
                    enabledMarkets.add(providerConfiguration.getMarket());
                }
        );
    }

    public boolean marketEnabled(String market) {
        return this.enabledMarkets.contains(market);
    }

    // return a new map, so the original remains unaltered, in case of any alterations.
    public Map<String, ProviderConfiguration> getProviderConfigurations() {
        return Maps.newHashMap(this.providerConfigurations);
    }

    public ProviderConfiguration getProviderConfiguration(String providerName) {
        return providerConfigurations.get(providerName);
    }
}
