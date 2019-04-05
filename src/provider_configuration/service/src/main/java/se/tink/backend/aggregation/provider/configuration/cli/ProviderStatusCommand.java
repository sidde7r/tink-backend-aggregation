package se.tink.backend.aggregation.provider.configuration.cli;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationCore;
import se.tink.backend.aggregation.provider.configuration.storage.ProviderConfigurationProvider;
import se.tink.libraries.provider.enums.ProviderStatuses;
import se.tink.libraries.cli.printutils.CliPrintUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProviderStatusCommand extends ProviderConfigurationCommand<ProviderServiceConfiguration> {

    private static final String STATUS_FIELD = "providerStatus";
    private static final String NAME_FIELD = "providerName";
    private static final String SHOW_FIELD = "showList";
    private static final String MARKET_FIELD = "market";
    private static final String CLUSTER_FIELD = "cluster";
    private static final String SET_FIELD = "setStatus";

    public ProviderStatusCommand() {
        super("provider-status",
                "Change status for a providers or list all current statuses.");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        Subparsers subparsers = subparser.addSubparsers();

        Subparser listProviders = subparsers.addParser("list")
                .description("List provider statuses")
                .setDefault(SHOW_FIELD, true);
        listProviders.addArgument("-m", "--market")
                .dest(MARKET_FIELD)
                .type(String.class)
                .required(false)
                .help("Market to list provider for");
        listProviders.addArgument("-c", "--cluster")
                .dest(CLUSTER_FIELD)
                .type(String.class)
                .required(true)
                .help("Cluster to list provider for");

        Subparser updateProviderstatus = subparsers.addParser("global_status")
                .description("Update provider status by name")
                .setDefault(SHOW_FIELD, false) // need to set default otherwise it's a null pointer
                .setDefault(SET_FIELD, true);

        updateProviderstatus.addArgument("-n", "--name")
                .dest(NAME_FIELD)
                .type(String.class)
                .required(true)
                .help("Provider name to change status of");

        updateProviderstatus.addArgument("-s", "--status")
                .dest(STATUS_FIELD)
                .type(ProviderStatuses.class)
                .required(true)
                .help("Status to change provider to");

        Subparser removeProviderStatus = subparsers.addParser("no_global_status")
                .description("Update provider status by name")
                .setDefault(SHOW_FIELD, false) // need to set default otherwise it's a null pointer
                .setDefault(SET_FIELD, false);

        removeProviderStatus.addArgument("-n", "--name")
                .dest(NAME_FIELD)
                .type(String.class)
                .required(true)
                .help("Provider name to change status of");
    }


    private ProviderConfigurationProvider createConfigurationProvider(Injector injector) throws IOException {
        return injector.getInstance(ProviderConfigurationProvider.class);
    }
    
    private void printProviderStatuses(Map<String, ProviderStatuses> providerStatuses){
        List<Map<String, String>> output = Lists.newArrayList();
        for (Map.Entry<String, ProviderStatuses> entry : providerStatuses.entrySet()) {
            output.add(CliPrintUtils.keyValueEntry(entry.getKey(), entry.getValue().toString()));
        }
        CliPrintUtils.printTable(output);
    }

    private void printProviderStatus(List<ProviderConfigurationCore> providerConfigurationCoreList) {
        Map<String, ProviderStatuses> providerStatusesList = providerConfigurationCoreList.stream()
                .collect(Collectors.toMap(ProviderConfigurationCore::getName, ProviderConfigurationCore::getStatus));
        printProviderStatuses(providerStatusesList);
    }

    @Override
    protected void run(Bootstrap<ProviderServiceConfiguration> bootstrap, Namespace namespace,
            ProviderServiceConfiguration configuration, Injector injector) throws Exception {

        ProviderStatuses providerStatus = namespace.get(STATUS_FIELD);
        String providerName = namespace.getString(NAME_FIELD);
        String market = namespace.get(MARKET_FIELD);
        String clusterId = namespace.get(CLUSTER_FIELD);


        ProviderConfigurationProvider configurationProvider = createConfigurationProvider(injector);

        if (namespace.getBoolean(SHOW_FIELD)){
            List<ProviderConfigurationCore> providerConfigurationCoreList;
            if (Objects.isNull(market)) {
                providerConfigurationCoreList = configurationProvider.findAllByClusterId(clusterId);
            } else {
                providerConfigurationCoreList = configurationProvider.findAllByClusterIdAndMarket(clusterId, market);
            }
            printProviderStatus(providerConfigurationCoreList);
            return;
        }

        if (namespace.getBoolean(SET_FIELD)) {
            configurationProvider.updateStatus(providerName, providerStatus);
            return;
        }

        configurationProvider.removeStatus(providerName);
    }
}
