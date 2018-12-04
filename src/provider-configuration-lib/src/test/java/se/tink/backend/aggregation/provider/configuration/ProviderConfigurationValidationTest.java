package se.tink.backend.aggregation.provider.configuration;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class ProviderConfigurationValidationTest extends ProviderConfigurationServiceTestBase {
    @Inject
    private @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName;
    @Inject
    private @Named("enabledProvidersOnCluster") Map<String, Set<String>> enabledProvidersOnCluster;
    @Inject
    private @Named("providerOverrideOnCluster") Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster;
    @Inject
    private @Named("capabilitiesByAgent") Map<String, Set<ProviderConfiguration.Capability>> providerAgentCapabilities;

    @Test
    public void validateAllAvailableProvidersForAClusterAreAvailableInConfigurations() {
        Map<String, List<String>> missingProvidersByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.get(clusterId);
            if (Objects.isNull(providerNamesForCluster) || providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> missingProviders = new ArrayList<>();
            providerNamesForCluster.forEach(providerName -> {
                ProviderConfiguration providerConfigurationForCluster = getProviderConfigurationForCluster(
                        clusterId, providerName);

                if (Objects.isNull(providerConfigurationForCluster)) {
                    missingProviders.add(providerName);
                }
            });

            if (missingProviders.isEmpty()) {
                continue;
            }

            missingProvidersByClusterId.put(clusterId, missingProviders);
        }

        assertThat(missingProvidersByClusterId.entrySet()).isEmpty();
    }

    private ProviderConfiguration getProviderConfigurationForCluster(String clusterId, String providerName) {
        if (!enabledProvidersOnCluster.containsKey(clusterId)) {
            return null;
        }

        if (!enabledProvidersOnCluster.get(clusterId).contains(providerName)) {
            return null;
        }

        if (!providerOverrideOnCluster.containsKey(clusterId)) {
            return providerConfigurationByName.get(providerName);
        }

        if (!providerOverrideOnCluster.get(clusterId).containsKey(providerName)) {
            return providerConfigurationByName.get(providerName);
        }
        
        return providerOverrideOnCluster.get(clusterId).get(providerName);
    }

    @Test
    public void validateMarketNotNull() {
        Map<String, List<String>> providersWithMarketNullByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.getOrDefault(
                    clusterId, Collections.emptySet());

            if (providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> providersWithMarketNull = providerNamesForCluster.stream()
                    .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                    .filter(Objects::nonNull)
                    .filter(providerConfiguration -> Objects.isNull(providerConfiguration.getMarket()))
                    .map(ProviderConfiguration::getName)
                    .collect(Collectors.toList());

            if (providersWithMarketNull.isEmpty()) {
                continue;
            }

            providersWithMarketNullByClusterId.put(clusterId, providersWithMarketNull);
        }

        assertThat(providersWithMarketNullByClusterId.entrySet()).isEmpty();
    }

    @Test
    public void validateCurrencyNotNull() {
        Map<String, List<String>> providersWithMarketNullByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.getOrDefault(
                    clusterId, Collections.emptySet());

            if (providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> providersWithMarketNull = providerNamesForCluster.stream()
                    .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                    .filter(Objects::nonNull)
                    .filter(providerConfiguration -> Objects.isNull(providerConfiguration.getCurrency()))
                    .map(ProviderConfiguration::getName)
                    .collect(Collectors.toList());

            if (providersWithMarketNull.isEmpty()) {
                continue;
            }

            providersWithMarketNullByClusterId.put(clusterId, providersWithMarketNull);
        }

        assertThat(providersWithMarketNullByClusterId.entrySet()).isEmpty();
    }

    public void verifyAllProviderCapabilitiesAreAvailableInAgentCapabilities() {
        Set<String> agentCapabilityClassNames = providerAgentCapabilities.keySet();

        for (String providerName : providerConfigurationByName.keySet()) {

            ProviderConfiguration providerConfiguration = providerConfigurationByName.get(providerName);
            if (Objects.isNull(providerConfiguration) || Objects.isNull(providerConfiguration.getClassName())) {
                continue;
            }

            agentCapabilityClassNames.remove(providerConfiguration.getClassName());
        }

        assertThat(agentCapabilityClassNames.isEmpty()).isTrue();
    }

    @Test
    public void verifyAllAgentCapabilitiesAreAvailableInProviderCapabilities() {
        Set<String> providerCapabilityClassNames = providerConfigurationByName.keySet().stream()
                .filter(provider -> Objects.nonNull(provider)
                        && Objects.nonNull(providerConfigurationByName.get(provider).getClassName()))
                .map(provider -> providerConfigurationByName.get(provider).getClassName())
                .collect(Collectors.toSet());
        Set<String> agentCapabilityClassNames = providerAgentCapabilities.keySet();

        providerCapabilityClassNames.removeAll(agentCapabilityClassNames);;
        assertThat(providerCapabilityClassNames.isEmpty()).isTrue();
    }
}
