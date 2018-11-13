package se.tink.backend.aggregation.provider.configuration.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.ProviderConfigurationProvider;
import se.tink.backend.aggregation.provider.configuration.storage.module.ProviderFileModule;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;
import se.tink.libraries.cli.printutils.CliPrintUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.List;
import java.util.Map;

public class DebugProviderCommand extends ProviderConfigurationCommand<ProviderServiceConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(DebugProviderCommand.class);

    public DebugProviderCommand() {
        super("debug-provider", "Dump debug info for a provider");
    }

    private void printProviderInfo(ProviderConfiguration provider) {
        List<Map<String, String>> output = Lists.newArrayList();

        output.add(CliPrintUtils.keyValueEntry("name", provider.getName()));
        output.add(CliPrintUtils.keyValueEntry("capabilities", provider.getCapabilitiesSerialized()));
        output.add(CliPrintUtils.keyValueEntry("classname", provider.getClassName()));
        output.add(CliPrintUtils.keyValueEntry("credentialstype", String.valueOf(provider.getCredentialsType())));
        output.add(CliPrintUtils.keyValueEntry("currency", provider.getCurrency()));
        output.add(CliPrintUtils.keyValueEntry("displaydescription", provider.getDisplayDescription()));
        output.add(CliPrintUtils.keyValueEntry("displayname", provider.getDisplayName()));
        output.add(CliPrintUtils.keyValueEntry("fields", SerializationUtils.serializeToString(provider.getFields())));
        output.add(CliPrintUtils.keyValueEntry("groupdisplayname", provider.getGroupDisplayName()));
        output.add(CliPrintUtils.keyValueEntry("market", provider.getMarket()));
        output.add(CliPrintUtils.keyValueEntry("multifactor", String.valueOf(provider.isMultiFactor())));
        output.add(CliPrintUtils.keyValueEntry("passwordhelptext", provider.getPasswordHelpText()));
        output.add(CliPrintUtils.keyValueEntry("payload", provider.getPayload()));
        output.add(CliPrintUtils.keyValueEntry("popular", String.valueOf(provider.isPopular())));
        output.add(CliPrintUtils.keyValueEntry("refreshfrequency", String.valueOf(provider.getRefreshFrequency())));
        output.add(CliPrintUtils.keyValueEntry("refreshfrequencyfactor", String.valueOf(provider.getRefreshFrequencyFactor())));
        output.add(CliPrintUtils.keyValueEntry("status", provider.getStatus().name()));
        output.add(CliPrintUtils.keyValueEntry("transactional", String.valueOf(provider.isTransactional())));
        output.add(CliPrintUtils.keyValueEntry("type", provider.getType().name()));

        CliPrintUtils.printTableLong(output);
    }

    @Override
    protected void run(Bootstrap<ProviderServiceConfiguration> bootstrap, Namespace namespace,
            ProviderServiceConfiguration configuration, Injector injector) throws Exception {

        final String providerName = System.getProperty("providerName");
        Preconditions.checkNotNull(providerName, "providerName must not be null.");
        final String clusterId = System.getProperty("clusterId");
        Preconditions.checkNotNull(clusterId, "clusterId must not be null.");

        ProviderConfigurationProvider providers = injector.getInstance(ProviderConfigurationProvider.class);
        ProviderConfiguration provider = providers.findByClusterIdAndProviderName(clusterId, providerName);

        if (provider == null) {
            log.warn("Provider {} not found in {}" , providerName, clusterId);
            return;
        }

        printProviderInfo(provider);
    }
}
