package se.tink.backend.aggregation.provider.configuration.cli;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import se.tink.backend.aggregation.provider.configuration.repositories.mysql.ProviderConfigurationRepository;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.cli.printutils.CliPrintUtils;

public class ProviderStatusCommand extends ProviderConfigurationCommand<ServiceConfiguration> {

    private static final String STATUS_FIELD = "providerStatus";
    private static final String NAME_FIELD = "providerName";
    private static final String SHOW_FIELD = "showList";
    private static final String MARKET_FIELD = "market";

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

        Subparser updateProvider = subparsers.addParser("update")
                .description("Update provider status by name")
                .setDefault(SHOW_FIELD, false);// need to set default otherwise it's a null pointer
        updateProvider.addArgument("-n", "--name")
                .dest(NAME_FIELD)
                .type(String.class)
                .required(true)
                .help("Provider name to change status of");

        updateProvider.addArgument("-s", "--status")
                .dest(STATUS_FIELD)
                .type(ProviderStatuses.class)
                .required(true)
                .help("Status to change provider to");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {

        ProviderStatuses providerStatus = namespace.get(STATUS_FIELD);
        String providerName = namespace.getString(NAME_FIELD);
        String market = namespace.get(MARKET_FIELD);

        boolean updateProviderStatus = !Strings.isNullOrEmpty(providerName) && providerStatus != null;

        ProviderConfigurationRepository providerConfigurationRepository = injector.getInstance(
                ProviderConfigurationRepository.class);
        if (updateProviderStatus) {
            new ProviderStatusUpdater(providerConfigurationRepository).update(providerName, providerStatus);
        }
        if (namespace.getBoolean(SHOW_FIELD)){
            new ProviderStatusesFetcher(providerConfigurationRepository, market).fetch(CliPrintUtils::printTable);
        }
    }
}
