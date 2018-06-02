package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import org.assertj.core.util.Lists;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderRefreshSchedule;
import se.tink.backend.system.cli.CliPrintUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DebugProviderCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(DebugProviderCommand.class);

    public DebugProviderCommand() {
        super("debug-provider", "Dump debug info for a provider");
    }

    private void printProviderInfo(Provider provider) {
        List<Map<String, String>> output = Lists.newArrayList();

        output.add(CliPrintUtils.keyValueEntry("name", provider.getName()));
        output.add(CliPrintUtils.keyValueEntry("capabilities", provider.getCapabilitiesSerialized()));
        output.add(CliPrintUtils.keyValueEntry("classname", provider.getClassName()));
        output.add(CliPrintUtils.keyValueEntry("credentialstype", String.valueOf(provider.getCredentialsType())));
        output.add(CliPrintUtils.keyValueEntry("currency", provider.getCurrency()));
        output.add(CliPrintUtils.keyValueEntry("displaydescription", provider.getDisplayDescription()));
        output.add(CliPrintUtils.keyValueEntry("displayname", provider.getDisplayName()));
        output.add(CliPrintUtils.keyValueEntry("email", provider.getEmail()));
        output.add(CliPrintUtils.keyValueEntry("fields", SerializationUtils.serializeToString(provider.getFields())));
        output.add(CliPrintUtils.keyValueEntry("groupdisplayname", provider.getGroupDisplayName()));
        output.add(CliPrintUtils.keyValueEntry("market", provider.getMarket()));
        output.add(CliPrintUtils.keyValueEntry("multifactor", String.valueOf(provider.isMultiFactor())));
        output.add(CliPrintUtils.keyValueEntry("passwordhelptext", provider.getPasswordHelpText()));
        output.add(CliPrintUtils.keyValueEntry("payload", provider.getPayload()));
        output.add(CliPrintUtils.keyValueEntry("phone", provider.getPhone()));
        output.add(CliPrintUtils.keyValueEntry("popular", String.valueOf(provider.isPopular())));
        output.add(CliPrintUtils.keyValueEntry("refreshfrequency", String.valueOf(provider.getRefreshFrequency())));
        output.add(CliPrintUtils.keyValueEntry("refreshfrequencyfactor", String.valueOf(provider.getRefreshFrequencyFactor())));
        output.add(CliPrintUtils.keyValueEntry("refreshschedule", getRefreshScheduleAsString(provider)));
        output.add(CliPrintUtils.keyValueEntry("status", provider.getStatus().name()));
        output.add(CliPrintUtils.keyValueEntry("transactional", String.valueOf(provider.isTransactional())));
        output.add(CliPrintUtils.keyValueEntry("tutorialUrl", provider.getTutorialUrl()));
        output.add(CliPrintUtils.keyValueEntry("type", provider.getType().name()));
        output.add(CliPrintUtils.keyValueEntry("url", provider.getUrl()));

        CliPrintUtils.printTable(output);
    }

    private String getRefreshScheduleAsString(Provider provider) {
        Optional<ProviderRefreshSchedule> refreshSchedule = provider.getRefreshSchedule();

        return refreshSchedule.isPresent() ? String.valueOf(refreshSchedule.get()) : null;
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final String providerName = System.getProperty("providerName");
        Preconditions.checkNotNull(providerName, "providerName must not be null.");

        ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);
        Provider provider = providerRepository.findByName(providerName);

        if (provider == null) {
            log.warn(String.format("Provider %s not found in database", providerName));
            return;
        }

        printProviderInfo(provider);
    }
}
