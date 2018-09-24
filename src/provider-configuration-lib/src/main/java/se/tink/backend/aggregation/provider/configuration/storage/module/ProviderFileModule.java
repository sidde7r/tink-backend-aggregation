package se.tink.backend.aggregation.provider.configuration.storage.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderOverrideOnClusterModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ClusterProviderListModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderConfigModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderSpecificationModel;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProviderFileModule extends AbstractModule {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ProviderFileModule.class);

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
    public Map<String, List<String>> provideEnabledProvidersForCluster() throws IOException {
        return loadEnabledProvidersOnClusterFromJson();
    }

    @Provides
    @Singleton
    @Named("providerOverrideOnCluster")
    public Map<String, Map<String, ProviderConfiguration>> provideClusterSpecificProviderConfiguraiton() throws IOException {
        return loadProviderOverrideOnClusterFromJson();
    }

    protected Map<String, ProviderConfiguration> loadProviderConfigurationFromJson() throws IOException {
        File directory = new File("data/seeding");
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

    protected Map<String, List<String>> loadEnabledProvidersOnClusterFromJson() throws IOException {
        String clusterProviderFilePath = "data/seeding/cluster-provider-configuration.json";
        File clusterProviderFile = new File(clusterProviderFilePath);
        ProviderOverrideOnClusterModel providerOverrideOnClusterModel =
                mapper.readValue(clusterProviderFile, ProviderOverrideOnClusterModel.class);

        List<ClusterProviderListModel> clusterProviderListModelList = providerOverrideOnClusterModel.getClusters();

        return clusterProviderListModelList.stream()
                .collect(Collectors.toMap(ClusterProviderListModel::getClusterId, ClusterProviderListModel::getProviderName));
    }

    protected Map<String, Map<String, ProviderConfiguration>> loadProviderOverrideOnClusterFromJson() throws IOException {
        File directory = new File("data/seeding");
        File[] providerSpecificationFiles = directory.listFiles((dir, fileName) -> fileName.matches("provider-specification.*.json"));
        Map<String, Map<String, ProviderConfiguration>> providerSpecificationByCluster = Maps.newHashMap();

        if (providerSpecificationFiles == null) {
            throw new IOException("no provider specification file found");
        }

        for (File providerSpecificationFile : providerSpecificationFiles) {
            log.info("Seeding provider specific from file {}", providerSpecificationFile.getName());
            parseProviderOverrideOnCluster(providerSpecificationFile, providerSpecificationByCluster);
        }
        return providerSpecificationByCluster;
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

    private void parseProviderOverrideOnCluster(File providerSpecificationFile, Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster)
            throws IOException {
        ProviderSpecificationModel providerSpecificationModel = mapper.readValue(providerSpecificationFile, ProviderSpecificationModel.class);

        String clusterId = providerSpecificationModel.getClusterId();
        List<ProviderConfiguration> providerSpecificConfiguration = providerSpecificationModel.getProviderSpecificConfiguration();

        Preconditions.checkNotNull(clusterId,
                "no cluster id found for provider specification file {}",providerSpecificationFile.getName());

        Map<String, ProviderConfiguration> providerConfigurationMap = Maps.newHashMap();

        if (providerSpecificConfiguration == null || providerSpecificConfiguration.isEmpty()){
            // assume these files can be empty when there is no provider specification for the cluster
            log.warn("No providers specified for file {}" , providerSpecificationFile.getName());
            return;
        }

        for (ProviderConfiguration providerConfiguration : providerSpecificConfiguration) {
            providerConfigurationMap.put(providerConfiguration.getName(), providerConfiguration);
        }

        providerOverrideOnCluster.put(clusterId, providerConfigurationMap);
        log.info("Seeded {} provider specification for cluster {} " , providerConfigurationMap.size(), clusterId);
    }
}
