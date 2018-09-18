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
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ClusterProviderConfigurationModel;
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
    @Named("clusterProviderList")
    public Map<String, List<String>> provideEnabledProvidersForCluster() throws IOException {
        return loadClusterEnabledProviderNamesFromJson();
    }

    @Provides
    @Singleton
    @Named("clusterSpecificProviderConfiguration")
    public Map<String, Map<String, ProviderConfiguration>> provideClusterSpecificProviderConfiguraiton() throws IOException {
        return loadClusterSpecificProviderConfigurationFromJson();
    }

    private Map<String, ProviderConfiguration> loadProviderConfigurationFromJson() throws IOException {
        File directory = new File("data/seeding");
        File[] providerFiles = directory.listFiles((dir, fileName) -> fileName.matches("providers-[a-z]{2}.json"));

        if (providerFiles == null) {
            throw new IOException("no provider file found");
        }

        Map<String, ProviderConfiguration> providerConfigurationByProviderName = Maps.newHashMap();

        for (File providerFile : providerFiles) {
            log.info("Seeding from file " + providerFile.getName());
            seedProvider(providerFile, providerConfigurationByProviderName);
        }

        return providerConfigurationByProviderName;
    }

    private Map<String, List<String>> loadClusterEnabledProviderNamesFromJson() throws IOException {
        String clusterProviderFilePath = "data/seeding/cluster-provider-configuration.json";
        File clusterProviderFile = new File(clusterProviderFilePath);
        ClusterProviderConfigurationModel clusterProviderConfigurationModel =
                mapper.readValue(clusterProviderFile, ClusterProviderConfigurationModel.class);

        List<ClusterProviderListModel> clusterProviderListModelList = clusterProviderConfigurationModel.getClusters();

        return clusterProviderListModelList.stream()
                .collect(Collectors.toMap(ClusterProviderListModel::getClusterId, ClusterProviderListModel::getProviderName));
    }

    private Map<String, Map<String, ProviderConfiguration>> loadClusterSpecificProviderConfigurationFromJson() throws IOException {
        File directory = new File("data/seeding");
        File[] providerSpecificationFiles = directory.listFiles((dir, fileName) -> fileName.matches("provider-specification.*.json"));
        Map<String, Map<String, ProviderConfiguration>> providerSpecificationByCluster = Maps.newHashMap();

        if (providerSpecificationFiles == null) {
            throw new IOException("no provider specification file found");
        }

        for (File providerSpecificationFile : providerSpecificationFiles) {
            log.info("Seeding provider specific from file %s", providerSpecificationFile.getName());
            seedProviderSpecification(providerSpecificationFile, providerSpecificationByCluster);
        }
        return providerSpecificationByCluster;
    }

    private void seedProvider(File providerFile, Map<String, ProviderConfiguration> providerConfigurationByProviderName)
            throws IOException, IllegalStateException {
        ProviderConfigModel providerConfig = mapper.readValue(providerFile, ProviderConfigModel.class);

        String currency = providerConfig.getCurrency();
        String market = providerConfig.getMarket();
        List<ProviderConfiguration> providers = providerConfig.getProviders();

        for (ProviderConfiguration providerConfiguration : providers) {
            Preconditions.checkNotNull(market,
                    "no market found for provider configuration file %s", providerFile.getName());
            Preconditions.checkNotNull(currency,
                    "no currency found for provider configuration file %s", providerFile.getName());
            providerConfiguration.setMarket(market);
            providerConfiguration.setCurrency(currency);
            providerConfigurationByProviderName.put(providerConfiguration.getName(), providerConfiguration);
        }
        log.info("Seeded %d providers for cluster %s " , providers.size(), market);
    }

    private void seedProviderSpecification(File providerSpecificationFile, Map<String, Map<String, ProviderConfiguration>> providerSpecificationByCluster)
            throws IOException {
        ProviderSpecificationModel providerSpecificationModel = mapper.readValue(providerSpecificationFile, ProviderSpecificationModel.class);

        String clusterId = providerSpecificationModel.getClusterId();
        List<ProviderConfiguration> providerSpecificConfiguration = providerSpecificationModel.getProviderSpecificConfiguration();

        Preconditions.checkNotNull(clusterId,
                "no cluster id found for provider specification file %s",providerSpecificationFile.getName());

        Map<String, ProviderConfiguration> providerConfigurationMap = Maps.newHashMap();

        if (providerSpecificConfiguration == null || providerSpecificConfiguration.isEmpty()){
            // assume these files can be empty when there is no provider specification for the cluster
            log.warn("No providers specified for file %s" , providerSpecificationFile.getName());
            return;
        }

        for (ProviderConfiguration providerConfiguration : providerSpecificConfiguration) {
            providerConfigurationMap.put(providerConfiguration.getName(), providerConfiguration);
        }

        providerSpecificationByCluster.put(clusterId, providerConfigurationMap);
        log.info("Seeded %d provider specification for cluster %s " , providerConfigurationMap.size(), clusterId);
    }
}
