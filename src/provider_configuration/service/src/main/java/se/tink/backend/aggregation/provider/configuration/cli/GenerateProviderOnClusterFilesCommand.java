package se.tink.backend.aggregation.provider.configuration.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.cli.util.ProviderConfigurationComparator;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ClusterProviderListModel;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderConfigWrapper;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.ProviderSpecificationModel;

/*
   run this command using bazel :
       bazel run --jvmopt="-Dclusterid=oxford-staging -Dpath=$(pwd)" -- :provider_configuration generate-provider-override-on-cluster etc/development-minikube-provider-configuration-server.yml
   note that this command will generate all files in the same directory. files will be looking like:
   available-providers-AT.json ...
   provider-override-AT.json ...
*/

public class GenerateProviderOnClusterFilesCommand
        extends ConfiguredCommand<ProviderServiceConfiguration> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log =
            LoggerFactory.getLogger(GenerateProviderOnClusterFilesCommand.class);
    private String RAW_PROVIDER_DATA_PATH;
    private String AVAILABLE_PROVIDERS_PATH;
    private String OVERRIDING_PROVIDERS_PATH;
    private final boolean testrun = false;

    public GenerateProviderOnClusterFilesCommand() {
        super(
                "generate-provider-override-on-cluster",
                "taking provider configuration on cluster from json, "
                        + "generate a json of provider that contains different fields");
    }

    private Map<String, List<ProviderConfigurationStorage>> generateProviderOverride(
            Map<String, Map<String, ProviderConfigurationStorage>>
                    providersFromClusterExportByMarket,
            Map<String, Map<String, ProviderConfigurationStorage>>
                    providersFromSeedingFilesByMarket) {

        Map<String, List<ProviderConfigurationStorage>> providerOverrideByMarket =
                Maps.newHashMap();

        for (Map.Entry<String, Map<String, ProviderConfigurationStorage>> entry :
                providersFromClusterExportByMarket.entrySet()) {
            String market = entry.getKey();
            Map<String, ProviderConfigurationStorage> providersFromCluster = entry.getValue();
            Map<String, ProviderConfigurationStorage> providersFromSeeding =
                    providersFromSeedingFilesByMarket.get(market);

            if (providersFromSeeding == null || providersFromSeeding.isEmpty()) {
                providerOverrideByMarket.put(
                        market, new ArrayList<>(providersFromCluster.values()));
                continue;
            }

            List<ProviderConfigurationStorage> providerOverrideOnMarket =
                    generateProviderOverridePerMarket(providersFromCluster, providersFromSeeding);
            log.info(
                    "different provider on market {}: {} ",
                    market,
                    providerOverrideOnMarket.size());
            providerOverrideByMarket.put(market, providerOverrideOnMarket);
        }

        return providerOverrideByMarket;
    }

    private List<ProviderConfigurationStorage> generateProviderOverridePerMarket(
            Map<String, ProviderConfigurationStorage> providersFromClusterExport,
            Map<String, ProviderConfigurationStorage> providersFromSeedingFiles) {

        List<ProviderConfigurationStorage> providerOverride = Lists.newArrayList();
        providersFromClusterExport.forEach(
                (name, providerFromCluster) -> {
                    ProviderConfigurationStorage providerFromFile =
                            providersFromSeedingFiles.get(name);

                    if (providerFromFile == null) {
                        providerOverride.add(providerFromCluster);
                        return;
                    }

                    if (!ProviderConfigurationComparator.equals(
                            providerFromFile, providerFromCluster)) {
                        providerOverride.add(providerFromCluster);
                    }
                });

        return providerOverride;
    }

    private Map<String, List<String>> generateAvailableProvidersOnCluster() throws IOException {
        File clusterExportFile = new File(RAW_PROVIDER_DATA_PATH);

        // read in a list of providers from the exported file from cluster
        List<ProviderConfigurationStorage> providers =
                mapper.readValue(
                        clusterExportFile,
                        new TypeReference<List<ProviderConfigurationStorage>>() {});

        Map<String, List<String>> availableProviders =
                providers.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ProviderConfigurationStorage::getMarket,
                                        Collectors.mapping(
                                                ProviderConfigurationStorage::getName,
                                                Collectors.toList())));

        availableProviders.forEach(
                (m, p) -> {
                    log.info("providers available in market {} : {} ", m, p.size());
                });

        return availableProviders;
    }

    private Map<String, Map<String, ProviderConfigurationStorage>> loadProvidersFromClusterExport()
            throws IOException {
        File clusterExportFile = new File(RAW_PROVIDER_DATA_PATH);

        // read in a list of providers from the exported file from cluster
        List<ProviderConfigurationStorage> providers =
                mapper.readValue(
                        clusterExportFile,
                        new TypeReference<List<ProviderConfigurationStorage>>() {});

        log.info("loaded {} providers from exported provider file ", providers.size());

        // map providers by market
        Map<String, List<ProviderConfigurationStorage>> providersByMarket =
                providers.stream()
                        .collect(Collectors.groupingBy(ProviderConfigurationStorage::getMarket));

        // in each market, map providers by name
        return providersByMarket.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> mapProviderConfigurationByProviderName(entry.getValue())));
    }

    private Map<String, Map<String, ProviderConfigurationStorage>>
            loadProvidersSeedingFilesByMarket() throws IOException {
        File directory = new File("data/seeding");
        File[] providerFiles =
                directory.listFiles((dir, fileName) -> fileName.matches("providers-[a-z]{2}.json"));

        if (providerFiles == null) {
            throw new IOException("no provider file found");
        }

        Map<String, Map<String, ProviderConfigurationStorage>> providersByMarket =
                Maps.newHashMap();

        for (File providerFile : providerFiles) {
            parseProviderConfigurationsFromSeedingFile(providerFile, providersByMarket);
        }

        return providersByMarket;
    }

    private void parseProviderConfigurationsFromSeedingFile(
            File providerFile,
            Map<String, Map<String, ProviderConfigurationStorage>> providerConfigurationByMarket)
            throws IOException, IllegalStateException {
        ProviderConfigWrapper providerConfig =
                mapper.readValue(providerFile, ProviderConfigWrapper.class);

        String currency = providerConfig.getCurrency();
        String market = providerConfig.getMarket();

        Preconditions.checkNotNull(
                market,
                "no market found for provider configuration file %s",
                providerFile.getName());
        Preconditions.checkNotNull(
                currency,
                "no currency found for provider configuration file %s",
                providerFile.getName());

        List<ProviderConfigurationStorage> providerConfigurationStorages =
                providerConfig.getProviders();
        log.info(
                "loaded {} providers from provider json file {}",
                providerConfigurationStorages.size(),
                providerFile.getName());

        // ensure each provider configuration uses the market and currency set for the market.
        providerConfigurationStorages.forEach(
                providerConfiguration -> {
                    providerConfiguration.setMarket(market);
                    providerConfiguration.setCurrency(currency);
                });

        // put provider map in global provider map by market
        providerConfigurationByMarket.put(
                market, mapProviderConfigurationByProviderName(providerConfigurationStorages));
    }

    private Map<String, ProviderConfigurationStorage> mapProviderConfigurationByProviderName(
            List<ProviderConfigurationStorage> providerConfigurationStorages) {

        Map<String, ProviderConfigurationStorage> providerConfigurationByProviderName =
                Maps.newHashMap();

        for (ProviderConfigurationStorage providerConfigurationStorage :
                providerConfigurationStorages) {
            String providerCapabilitySerialized =
                    providerConfigurationStorage.getCapabilitiesSerialized();

            if (providerCapabilitySerialized == null
                    || providerCapabilitySerialized.equals("null")
                    || providerCapabilitySerialized.equals("[]")) {
                providerConfigurationStorage.setCapabilities(Sets.newHashSet());
            }

            providerConfigurationByProviderName.put(
                    providerConfigurationStorage.getName(), providerConfigurationStorage);
        }

        return providerConfigurationByProviderName;
    }

    private void writeMarketOverrideToFile(
            Map<String, List<ProviderConfigurationStorage>> overrideProvidersByMarket,
            String clusterId)
            throws IOException {

        for (Map.Entry<String, List<ProviderConfigurationStorage>> entry :
                overrideProvidersByMarket.entrySet()) {
            String market = entry.getKey();
            List<ProviderConfigurationStorage> overrideProviders = entry.getValue();

            if (overrideProviders.isEmpty()) {
                continue;
            }

            ProviderSpecificationModel providerSpecification = new ProviderSpecificationModel();
            providerSpecification.setClusterId(clusterId);
            providerSpecification.setProviderSpecificConfiguration(
                    Lists.newArrayList(overrideProviders));
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String out =
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(providerSpecification);

            writeToFile(
                    out,
                    OVERRIDING_PROVIDERS_PATH + "/provider-override-" + market + ".json",
                    "provider override");
        }
    }

    private void writeMarketAvailableProvidersToFile(
            Map<String, List<String>> providersAvailableByMarket, String clusterId)
            throws IOException {
        for (Map.Entry<String, List<String>> entry : providersAvailableByMarket.entrySet()) {
            String market = entry.getKey();
            List<String> availableProviders = entry.getValue();

            if (availableProviders.isEmpty()) {
                continue;
            }

            ClusterProviderListModel clusterProviderList = new ClusterProviderListModel();
            clusterProviderList.setClusterId(clusterId);
            clusterProviderList.setProviderName(Lists.newArrayList(availableProviders));
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            DefaultPrettyPrinter defaultPrettyPrinter = new DefaultPrettyPrinter();
            defaultPrettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            mapper.setDefaultPrettyPrinter(defaultPrettyPrinter);
            String out =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(clusterProviderList);

            writeToFile(
                    out,
                    AVAILABLE_PROVIDERS_PATH + "/available-providers-" + market + ".json",
                    "available providers");
        }
    }

    private void writeToFile(String out, String filepath, String fileContent) throws IOException {
        File file = new File(filepath);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        try {
            fw.write(out);
            fw.write("\n");
        } finally {
            fw.close();
        }

        log.info("Exported {} configurations to: {} ", fileContent, file.getAbsolutePath());
    }

    private void setPaths(String clusterid, String projectPath) {
        RAW_PROVIDER_DATA_PATH =
                System.getProperty("user.dir")
                        + "/providerdata/raw/"
                        + clusterid
                        + ".providers.json";
        if (testrun) {
            AVAILABLE_PROVIDERS_PATH = System.getProperty("user.dir") + "/../available-providers/";
            OVERRIDING_PROVIDERS_PATH =
                    System.getProperty("user.dir") + "/../overriding-providers/";
        } else {
            AVAILABLE_PROVIDERS_PATH =
                    projectPath + "/data/seeding/providers/available-providers/" + clusterid;
            OVERRIDING_PROVIDERS_PATH =
                    projectPath + "/data/seeding/providers/overriding-providers/" + clusterid;
        }
    }

    private void createDirectories(String dir) throws IOException {
        File file = new File(dir);
        if (file.exists()) {
            log.warn("directory already exist, file already exported and they will be overridden");
            return;
        }

        if (!new File(dir).mkdir()) {
            throw new IOException("can not create path " + dir);
        }
    }

    @Override
    protected void run(
            Bootstrap<ProviderServiceConfiguration> bootstrap,
            Namespace namespace,
            ProviderServiceConfiguration providerServiceConfiguration)
            throws Exception {
        String clusterId = System.getProperty("clusterid");
        String projectPath = System.getProperty("path");
        setPaths(clusterId, projectPath);
        createDirectories(AVAILABLE_PROVIDERS_PATH);
        createDirectories(OVERRIDING_PROVIDERS_PATH);

        Map<String, Map<String, ProviderConfigurationStorage>> providersFromSeedingFiles =
                loadProvidersSeedingFilesByMarket();

        Map<String, Map<String, ProviderConfigurationStorage>> providersFromClusterExport =
                loadProvidersFromClusterExport();

        Map<String, List<ProviderConfigurationStorage>> overrideProvidersByMarket =
                generateProviderOverride(providersFromClusterExport, providersFromSeedingFiles);

        Map<String, List<String>> providersAvailableByMarket =
                generateAvailableProvidersOnCluster();

        writeMarketOverrideToFile(overrideProvidersByMarket, clusterId);

        writeMarketAvailableProvidersToFile(providersAvailableByMarket, clusterId);
    }
}
