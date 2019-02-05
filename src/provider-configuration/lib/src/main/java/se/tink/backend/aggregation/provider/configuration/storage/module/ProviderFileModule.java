package se.tink.backend.aggregation.provider.configuration.storage.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.AgentCapabilitiesMapModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ClusterProviderListModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderConfigModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderSpecificationModel;


import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProviderFileModule extends AbstractModule {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ProviderFileModule.class);
    private static final String GLOBAL_PROVIDER_FILE_PATH = "data/seeding";
    private static final String AVAILABLE_PROVIDERS_FILE_PATH = "data/seeding/providers/available-providers";
    private static final String PROVIDER_OVERRIDE_FILE_PATH = "data/seeding/providers/overriding-providers";
    private static final String AGENT_CAPABILITIES_FILE_PATH = "data/seeding/providers/capabilities";
    private static final String LOCAL_DEVELOPMENT_KEY_STRING = "local-development";

    @Override
    protected void configure() { }

    @Provides
    @Singleton
    @Named("providerConfiguration")
    public Map<String, ProviderConfiguration> providerConfigurationByProviderName() throws IOException {
        return loadProviderConfigurationFromJson();
    }

    @Provides
    @Singleton
    @Named("enabledProvidersOnCluster")
    public Map<String, Set<String>> provideEnabledProvidersForCluster() throws IOException {
        return loadEnabledProvidersOnClusterFromJson();
    }

    @Provides
    @Singleton
    @Named("providerOverrideOnCluster")
    public Map<String, Map<String, ProviderConfiguration>> provideClusterSpecificProviderConfiguration() throws IOException {
        return loadProviderOverrideOnClusterFromJson();
    }

    @Provides
    @Singleton
    @Named("capabilitiesByAgent")
    public Map<String, Set<ProviderConfiguration.Capability>> provideAgentCapabilities() throws IOException {
        return loadAgentCapabilities();
    }

    protected Map<String, ProviderConfiguration> loadProviderConfigurationFromJson() throws IOException {
        File directory = new File(GLOBAL_PROVIDER_FILE_PATH);
        File[] providerFiles = directory.listFiles((dir, fileName) -> fileName.matches("providers-[a-z]{2}.json"));

        if (providerFiles == null) {
            throw new IOException("no provider file found");
        }

        Map<String, ProviderConfiguration> providerConfigurationByProviderName = Maps.newHashMap();

        for (File providerFile : providerFiles) {
            log.info("Seeding from file {}", providerFile.getName());
            parseProviderConfigurations(providerFile, providerConfigurationByProviderName);
        }

        return providerConfigurationByProviderName;
    }

    protected Map<String, Set<String>> loadEnabledProvidersOnClusterFromJson() throws IOException {

        File[] availableProviderDirectories = new File(AVAILABLE_PROVIDERS_FILE_PATH).listFiles();

        Preconditions.checkNotNull(availableProviderDirectories,
                "no available path found for loading available providers on cluster");

        Map<String, Set<String>> availableProvidersByCluster = Maps.newHashMap();

        for(File availableProviderDirectory : availableProviderDirectories){
            File[] availableProviderFiles = availableProviderDirectory.listFiles(
                    (dir, fileName) -> fileName.matches("available-providers-[A-Z]{2}.json"));
            Preconditions.checkNotNull(availableProviderFiles,
                    "no available file found for loading available providers on cluster in path {}",
                    availableProviderDirectory.getName());

            parseAvailableProvidersOnCluster(availableProviderFiles, availableProvidersByCluster);
        }

        enableAllProvidersForLocalDevelopment(availableProvidersByCluster);
        return availableProvidersByCluster;
    }

    protected Map<String, Map<String, ProviderConfiguration>> loadProviderOverrideOnClusterFromJson() throws IOException {

        File[] overridingProviderDirectories = new File(PROVIDER_OVERRIDE_FILE_PATH).listFiles();
        Preconditions.checkNotNull(overridingProviderDirectories,
                "no available path found for loading overriding providers on cluster");

        Map<String, Map<String, ProviderConfiguration>> overridingProvidersByCluster = Maps.newHashMap();

        for (File overridingProviderDirectory : overridingProviderDirectories) {
            File[] availableProviderFiles = overridingProviderDirectory.listFiles(
                    (dir, fileName) -> fileName.matches("provider-override-[A-Z]{2}.json"));
            Preconditions.checkNotNull(availableProviderFiles,
                    "no available file found for loading overriding providers on cluster in path {}",
                    overridingProviderDirectory.getName());

            parseOverridingProvidersOnCluster(availableProviderFiles, overridingProvidersByCluster);
        }
        loadEmptyOverrideForLocalDevelopment(overridingProvidersByCluster);
        return overridingProvidersByCluster;
    }

    private void loadEmptyOverrideForLocalDevelopment(Map<String, Map<String, ProviderConfiguration>> overridingProvidersOnCluster){
        overridingProvidersOnCluster.put(LOCAL_DEVELOPMENT_KEY_STRING, Collections.emptyMap());
    }

    private void enableAllProvidersForLocalDevelopment(Map<String, Set<String>> availableProvidersByCluster) throws IOException{
        Set<String> providerNames = loadProviderConfigurationFromJson().values().stream()
                .map(ProviderConfiguration::getName)
                .collect(Collectors.toSet());

        availableProvidersByCluster.put(LOCAL_DEVELOPMENT_KEY_STRING, providerNames);
    }
    
    private void parseAvailableProvidersOnCluster(
            File[] availableProviderFiles, Map<String, Set<String>> providersAvailableOnCluster) throws IOException{

        Set<String> availableProviderNames = Sets.newHashSet();
        String clusterId = null;

        for (File availableProviderFile : availableProviderFiles) {
            ClusterProviderListModel providerOverrideOnClusterModel =
                    mapper.readValue(availableProviderFile, ClusterProviderListModel.class);

            String clusterIdInFile = providerOverrideOnClusterModel.getClusterId();

            Preconditions.checkNotNull(clusterIdInFile,
                    "no available cluster id for file {}", availableProviderFile.getName());

            if (clusterId == null){
                clusterId = clusterIdInFile;
            }

            Preconditions.checkState(Objects.equals(clusterId, clusterIdInFile),
                    "wrong cluster id set in file {}, which cluster is this intended for?",
                    availableProviderFile.getAbsolutePath());

            List<String> providerNames = providerOverrideOnClusterModel.getProviderName();

            log.info("found {} providers for cluster {} in from file {}",
                    providerNames.size(), clusterId, availableProviderFile.getName());

            availableProviderNames.addAll(providerNames);
        }

        providersAvailableOnCluster.put(clusterId, availableProviderNames);

    }

    private void parseOverridingProvidersOnCluster(
            File[] overridingProviderFiles,
            Map<String, Map<String, ProviderConfiguration>> overridingProvidersOnCluster) throws IOException{

        String clusterId = null;
        Map<String, ProviderConfiguration> providerConfigurationMap = Maps.newHashMap();

        for (File overridingProviderFile : overridingProviderFiles) {
            ProviderSpecificationModel providerOverrideOnClusterModel =
                    mapper.readValue(overridingProviderFile, ProviderSpecificationModel.class);

            String clusterIdInFile = providerOverrideOnClusterModel.getClusterId();

            Preconditions.checkNotNull(clusterIdInFile,
                    "no available cluster id for file {}", overridingProviderFile.getName());

            if (clusterId == null){
                clusterId = clusterIdInFile;
            }

            Preconditions.checkState(Objects.equals(clusterId, clusterIdInFile),
                    "wrong cluster id set in file {}, which cluster is this intended for?",
                    overridingProviderFile.getAbsolutePath());

            List<ProviderConfiguration> providersOnCluster =
                    providerOverrideOnClusterModel.getProviderSpecificConfiguration();

            log.info("{} provider overriding for cluster {} in from file {}",
                    providersOnCluster.size(), clusterId, overridingProviderFile.getName());

            providersOnCluster.forEach(providerConfig ->
                    providerConfigurationMap.put(providerConfig.getName(), providerConfig));
        }
        overridingProvidersOnCluster.put(clusterId, providerConfigurationMap);

    }

    private void parseProviderConfigurations(File providerFile, Map<String, ProviderConfiguration> providerConfigurationByProviderName)
            throws IOException, IllegalStateException {
        ProviderConfigModel providerConfig = mapper.readValue(providerFile, ProviderConfigModel.class);

        String currency = providerConfig.getCurrency();
        String market = providerConfig.getMarket();
        List<ProviderConfiguration> providers = providerConfig.getProviders();

        for (ProviderConfiguration providerConfiguration : providers) {
            Preconditions.checkNotNull(market,
                    "no market found for provider configuration file {}", providerFile.getName());
            Preconditions.checkNotNull(currency,
                    "no currency found for provider configuration file {}", providerFile.getName());
            providerConfiguration.setMarket(market);
            providerConfiguration.setCurrency(currency);
            providerConfigurationByProviderName.put(providerConfiguration.getName(), providerConfiguration);
        }
        log.info("Seeded {} providers for cluster {} " , providers.size(), market);
    }

    private AgentCapabilitiesMapModel loadAgentCapabilities() throws IOException {
        File directory = new File(AGENT_CAPABILITIES_FILE_PATH);
        File[] agentCapabilities = directory.listFiles(
                (dir, fileName) -> fileName.matches("agent-capabilities.json"));

        Preconditions.checkNotNull(agentCapabilities,"Capabilities must be provided.");

        if (agentCapabilities.length != 1) {
            log.warn("Found more than one file with agent capabilities. Only first file in array will be processed.");
        }

        return mapper.readValue(agentCapabilities[0], AgentCapabilitiesMapModel.class);
    }
}
