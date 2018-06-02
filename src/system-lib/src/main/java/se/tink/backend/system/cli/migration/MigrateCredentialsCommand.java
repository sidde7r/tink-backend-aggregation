package se.tink.backend.system.cli.migration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.MigrateCredentialsRequest;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class MigrateCredentialsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(MigrateCredentialsCommand.class);
    private static final String DRY_RUN = "dryRun";
    private static final ImmutableSet<CredentialsTypes> WHITELISTED_TYPES = ImmutableSet.of(CredentialsTypes.KEYFOB,
            CredentialsTypes.PASSWORD, CredentialsTypes.MOBILE_BANKID);

    private static final Predicate<Credentials> IS_WHITELISTED_CREDENTIALS_TYPE = c ->
            WHITELISTED_TYPES.contains(c.getType());

    public MigrateCredentialsCommand() {
        super("migrate-credentials-to-cluster", "Migrate credentials to use the cluster encryption");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        final AtomicInteger userCounter = new AtomicInteger();
        final AtomicInteger credentialsCounter = new AtomicInteger();
        final CredentialsRepository credentialsRepository = injector.getInstance(CredentialsRepository.class);
        final ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);
        final UserRepository userRepository = injector.getInstance(UserRepository.class);

        final ImmutableMap<String, Provider> providersByName = Maps.uniqueIndex(providerRepository.findAll(),
                Provider::getName);

        final boolean dryRun = Boolean.getBoolean(DRY_RUN);

        AggregationControllerCommonClient aggregationControllerClient = injector.getInstance(
                AggregationControllerCommonClient.class);

        log.info(String.format("Starting credentials migration. Dry run: [%s]", String.valueOf(dryRun)));

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(4))
                .forEach(user -> migrateAllCredentialsForUser(userCounter, credentialsCounter, dryRun,
                        aggregationControllerClient, credentialsRepository, providersByName, user));
        log.info(String.format(
                "Finished credentials migration. Dry run: [%s] - Processed: [%s] users - Migrated: [%s] credentials.",
                String.valueOf(dryRun), userCounter.get(), credentialsCounter.get()));
    }

    private void migrateAllCredentialsForUser(AtomicInteger userCounter, AtomicInteger credentialsCounter,
            boolean dryRun, AggregationControllerCommonClient aggregationControllerCommonClient,
            CredentialsRepository credentialsRepository, ImmutableMap<String, Provider> providersByName, User user) {
        int handledUsers = userCounter.getAndIncrement();
        if (handledUsers % 10000 == 0) {
            log.info(String.format("Processed [%s] users", handledUsers));
        }

        List<Credentials> userCredentials = credentialsRepository.findAllByUserId(user.getId());

        userCredentials.stream()
                .filter(IS_WHITELISTED_CREDENTIALS_TYPE)
                .forEach(credentials -> {
                    if (credentials.getSensitiveDataSerialized() != null) {
                        return;
                    }

                    if (!dryRun) {
                        aggregationControllerCommonClient.migrateCredentials(
                                new MigrateCredentialsRequest(user, providersByName.get(credentials.getProviderName()),
                                        credentials));
                    }

                    credentialsCounter.incrementAndGet();
                });
    }
}
