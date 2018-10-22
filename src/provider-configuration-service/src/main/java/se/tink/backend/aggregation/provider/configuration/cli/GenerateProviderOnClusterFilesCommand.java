package se.tink.backend.aggregation.provider.configuration.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.cli.util.ProviderConfigurationComparator;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ClusterProviderListModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderConfigModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderSpecificationModel;
import se.tink.backend.common.config.ServiceConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
    run this command using bazel :
        bazel run :provider-configuration --jvmopt='-Dinput={provider file downloaded from cluster}
        -Doutput={where to store result files}' generate-provider-override-on-cluster etc/development-minikube-provider-configuration-server.yml
    note that this command will generate all files in the same directory. files will be looking like:
    available-providers-AT.json ...
    provider-override-AT.json ...

    the content of the files needs to be further processed before we can upload them.
    TODO: script for formatting the file needs to uploaded
    TODO: unit test for the generating difference. So other team member can investigate if the data quality is validated
 */

public class GenerateProviderOnClusterFilesCommand extends ConfiguredCommand<ServiceConfiguration> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(GenerateProviderOnClusterFilesCommand.class);

    public GenerateProviderOnClusterFilesCommand() {
        super("generate-provider-override-on-cluster", "taking provider configuration on cluster from json, " +
                "generate a json of provider that contains different fields");;
    }

    private Map<String, List<ProviderConfiguration>> generateProviderOverride(
            Map<String, Map<String, ProviderConfiguration>> providersFromClusterExportByCluster,
            Map<String, Map<String, ProviderConfiguration>> providersFromSeedingFilesByCluster){

        Map<String, List<ProviderConfiguration>> providerOverrideByMarket = Maps.newHashMap();

        for (Map.Entry<String, Map<String, ProviderConfiguration>> entry : providersFromClusterExportByCluster.entrySet()){
            String market = entry.getKey();
            Map<String, ProviderConfiguration> providersFromCluster = entry.getValue();
            Map<String, ProviderConfiguration> providersFromSeeding = providersFromSeedingFilesByCluster.get(market);

            if (providersFromSeeding == null || providersFromSeeding.isEmpty()) {
                providerOverrideByMarket.put(market, new ArrayList<>(providersFromCluster.values()));
            } else {
                List<ProviderConfiguration> providerOverrideOnMarket =
                        generateProviderOverridePerMarket(providersFromCluster, providersFromSeeding);
                log.info("different provider on market {}: {} ", market, providerOverrideOnMarket.size());
                providerOverrideByMarket.put(market, providerOverrideOnMarket);
            }
        }

        return providerOverrideByMarket;
    }

    private List<ProviderConfiguration> generateProviderOverridePerMarket(
            Map<String, ProviderConfiguration> providersFromClusterExport,
            Map<String, ProviderConfiguration> providersFromSeedingFiles){

        List<ProviderConfiguration> providerOverride = Lists.newArrayList();
        providersFromClusterExport.forEach(
                (name, providerFromCluster) -> {
                    ProviderConfiguration providerFromFile = providersFromSeedingFiles.get(name);
                    if (providerFromFile == null) {
                        providerOverride.add(providerFromCluster);
                    } else {
                        if ( !ProviderConfigurationComparator.equals(providerFromFile, providerFromCluster)){
                            providerOverride.add(providerFromCluster);
                        }
                    }
                }
        );
        return providerOverride;
    }


    private Map<String, List<String>> generateAvailableProvidersOnCluster(String filePath) throws IOException {
        File clusterExportFile = new File(filePath);

        // read in a list of providers from the exported file from cluster
        List<ProviderConfiguration> providers =
                mapper.readValue(clusterExportFile, new TypeReference<List<ProviderConfiguration>>() {});

        Map<String, List<String>> availableProviders =  providers.stream()
                .collect(Collectors.groupingBy(ProviderConfiguration::getMarket,
                        Collectors.mapping(ProviderConfiguration::getName, Collectors.toList())));
        availableProviders.forEach((m, p) -> {
            log.info("providers available in market {} : {} ", m , p.size());
        });
        return availableProviders;
    }

    private Map<String, Map<String, ProviderConfiguration>> loadProvidersFromClusterExport(String filePath)
            throws IOException{
        File clusterExportFile = new File(filePath);

        // read in a list of providers from the exported file from cluster
        List<ProviderConfiguration> providers =
                mapper.readValue(clusterExportFile, new TypeReference<List<ProviderConfiguration>>() {});

        log.info("loaded {} providers from exported provider file " , providers.size());

        // map providers by market
        Map<String, List<ProviderConfiguration>> providersByMarket =
                providers.stream().collect(Collectors.groupingBy(ProviderConfiguration::getMarket));

        // in each market, map providers by name
        return providersByMarket.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> mapProviderConfigurationByProviderName(entry.getValue())));
    }

    private Map<String, Map<String, ProviderConfiguration>> loadProvidersSeedingFilesByMarket() throws IOException{
        File directory = new File("data/seeding");
        File[] providerFiles = directory.listFiles((dir, fileName) -> fileName.matches("providers-[a-z]{2}.json"));

        if (providerFiles == null) {
            throw new IOException("no provider file found");
        }

        Map<String, Map<String, ProviderConfiguration>> providersByMarket = Maps.newHashMap();

        for (File providerFile : providerFiles) {
            parseProviderConfigurationsFromSeedingFile(providerFile, providersByMarket);
        }

        return providersByMarket;
    }

    private void parseProviderConfigurationsFromSeedingFile(File providerFile,
                                             Map<String, Map<String, ProviderConfiguration>>
                                                     providerConfigurationByMarket)
            throws IOException, IllegalStateException {
        ProviderConfigModel providerConfig = mapper.readValue(providerFile, ProviderConfigModel.class);

        String currency = providerConfig.getCurrency();
        String market = providerConfig.getMarket();

        Preconditions.checkNotNull(market,
                "no market found for provider configuration file %s", providerFile.getName());
        Preconditions.checkNotNull(currency,
                "no currency found for provider configuration file %s", providerFile.getName());

        List<ProviderConfiguration> providerConfigurations = providerConfig.getProviders();
        log.info("loaded {} providers from provider json file {}" , providerConfigurations.size(), providerFile.getName());

        // ensure each provider configuration uses the market and currency set for the market.
        providerConfigurations.forEach(providerConfiguration -> {
            providerConfiguration.setMarket(market);
            providerConfiguration.setCurrency(currency);
        });

        // put provider map in global provider map by market
        providerConfigurationByMarket.put(market, mapProviderConfigurationByProviderName(providerConfigurations));
    }

    private Map<String, ProviderConfiguration> mapProviderConfigurationByProviderName(
            List<ProviderConfiguration> providerConfigurations){
        Map<String, ProviderConfiguration> providerConfigurationByProviderName = Maps.newHashMap();
        for (ProviderConfiguration providerConfiguration : providerConfigurations){
            String providerCapabilitySerialized = providerConfiguration.getCapabilitiesSerialized();

            if (providerCapabilitySerialized == null
                    || providerCapabilitySerialized.equals("null")
                    || providerCapabilitySerialized.equals("[]")){
                    providerConfiguration.setCapabilities(Sets.newHashSet());
            }

            providerConfigurationByProviderName.put(providerConfiguration.getName(), providerConfiguration);
        }

        return providerConfigurationByProviderName;
    }

    private void writeMarketOverrideToFile(Map<String, List<ProviderConfiguration>> overrideProvidersByMarket,
                                           String dir, String clusterId) throws IOException {

        for (Map.Entry<String, List<ProviderConfiguration>> entry: overrideProvidersByMarket.entrySet()) {
            String market = entry.getKey();
            List<ProviderConfiguration> overrideProviders = entry.getValue();

            if (overrideProviders.isEmpty()) {
                continue;
            }

            ProviderSpecificationModel providerSpecification = new ProviderSpecificationModel();
            providerSpecification.setClusterId(clusterId);
            providerSpecification.setProviderSpecificConfiguration(overrideProviders);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            mapper.disable(MapperFeature.USE_ANNOTATIONS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writerWithDefaultPrettyPrinter().writeValue(byteStream, providerSpecification);
            writeToFile(byteStream, dir + "" + market+".json", "provider override");
        }
    }

    private void writeMarketAvailableProvidersToFile(Map<String,List<String>> providersAvailableByMarket,
                                                     String dir, String clusterId) throws IOException {
        for (Map.Entry<String, List<String>> entry: providersAvailableByMarket.entrySet()) {
            String market = entry.getKey();
            List<String> availableProviders = entry.getValue();

            if (availableProviders.isEmpty()) {
                continue;
            }

            ClusterProviderListModel clusterProviderList = new ClusterProviderListModel();
            clusterProviderList.setClusterId(clusterId);
            clusterProviderList.setProviderName(availableProviders);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            mapper.disable(MapperFeature.USE_ANNOTATIONS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writerWithDefaultPrettyPrinter().writeValue(byteStream, clusterProviderList);
            writeToFile(byteStream, dir + "" + market+".json", "available providers");
        }
    }

    private void writeToFile(ByteArrayOutputStream out, String filepath, String fileContent) throws IOException {
        File file = new File(filepath);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        try {
            fw.write(out.toString());
        } finally {
            fw.close();
        }

        log.info("Exported {} configurations to: {} ", fileContent, file.getAbsolutePath());
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace, ServiceConfiguration ServiceConfiguration) throws Exception {
        final String inputExportFile = System.getProperty("input");
        final String outputFilePath = System.getProperty("output");
        final String clusterId = System.getProperty("clusterId");
        log.info("input file path "+ inputExportFile);
        final String overrideFilePath = outputFilePath + "/overriding-providers/provider-override-";
        final String availableProvidersFilePath = outputFilePath + "/available-providers/available-providers-";

        Map<String, Map<String, ProviderConfiguration>> providersFromSeedingFiles =
                loadProvidersSeedingFilesByMarket();

        Map<String, Map<String, ProviderConfiguration>> providersFromClusterExport =
                loadProvidersFromClusterExport(inputExportFile);

        Map<String, List<ProviderConfiguration>> overrideProvidersByMarket =
                generateProviderOverride(providersFromClusterExport, providersFromSeedingFiles);

        Map<String, List<String>> providersAvailableByMarket =
                generateAvailableProvidersOnCluster(inputExportFile);

        writeMarketOverrideToFile(overrideProvidersByMarket, overrideFilePath, clusterId);

        writeMarketAvailableProvidersToFile(providersAvailableByMarket, availableProvidersFilePath, clusterId);
    }
}
