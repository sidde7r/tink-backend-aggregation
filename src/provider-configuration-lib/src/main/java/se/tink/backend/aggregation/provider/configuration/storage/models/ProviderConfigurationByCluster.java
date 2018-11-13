package se.tink.backend.aggregation.provider.configuration.storage.models;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Map<String, ProviderConfiguration> allProviderConfiguration) {
        this.providerConfigurations = Maps.newHashMap();
        this.enabledMarkets = new HashSet<String>();
        this.clusterId = clusterId;
        enabledProviders.forEach(
                providerName -> {
                        ProviderConfiguration providerConfiguration;
                        if (providerConfigurationOverrides
                                .containsKey(providerName)) {
                            providerConfiguration = providerConfigurationOverrides.get(providerName);
                        } else if (allProviderConfiguration
                                .containsKey(providerName)) {
                            providerConfiguration = allProviderConfiguration.get(providerName);
                        } else {
                            logger.error("Could not find configuration for enabled provider {} and cluster {}", providerName, clusterId);
                            return;
                        }
                        providerConfigurations.put(providerName, providerConfiguration);
                        enabledMarkets.add(providerConfiguration.getMarket());
                }
        );
    }

    public Map<String, ProviderConfiguration> getProviderConfigurations() {
        return this.providerConfigurations;
    }

    public boolean marketEnabled(String market) {
        return this.enabledMarkets.contains(market);
    }

    public ProviderConfiguration getProviderConfiguration(String providerName) {
        return providerConfigurations.get(providerName);
    }
}
