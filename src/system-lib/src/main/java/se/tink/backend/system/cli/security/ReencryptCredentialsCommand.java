package se.tink.backend.system.cli.security;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.ReencryptionRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class ReencryptCredentialsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(ReencryptCredentialsCommand.class);

    public ReencryptCredentialsCommand() {
        super("reencrypt-credentials", "Re-encrypts all credentials secrets using the latest inject key.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        final AggregationControllerCommonClient aggregationControllerCommonClient = serviceContext
                .getAggregationControllerCommonClient();
        final ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);

        final ImmutableMap<String, Provider> providersByName;
        if (serviceContext.isProvidersOnAggregation()) {
            providersByName = Maps.uniqueIndex(aggregationControllerCommonClient.listProviders(), Provider::getName);
        } else {
            providersByName = Maps.uniqueIndex(providerRepository.findAll(), Provider::getName);
        }

        if (serviceContext.isUseAggregationController()) {
            userRepository.streamAll()
                    .compose(new CommandLineInterfaceUserTraverser(4))
                    .forEach(user -> reencryptAllCredentialsForUser(
                            aggregationControllerCommonClient, credentialsRepository,
                            providersByName, user));
        } else {
            userRepository.streamAll()
                    .compose(new CommandLineInterfaceUserTraverser(4))
                    .forEach(user -> reencryptAllCredentialsForUser(serviceContext.getAggregationServiceFactory(),
                            credentialsRepository, providersByName, user));
        }
    }

    private static void reencryptAllCredentialsForUser(AggregationServiceFactory aggregationServiceFactory,
            CredentialsRepository credentialsRepository,
            ImmutableMap<String, Provider> providerByName, User user) {
        List<Credentials> userCredentials = credentialsRepository.findAllByUserId(user.getId());

        for (Credentials credentials : userCredentials) {
            log.info(credentials, "Requesting secret key rotation.");

            ReencryptionRequest request = new ReencryptionRequest();
            request.setCredentials(CoreCredentialsMapper.toAggregationCredentials(credentials));
            request.setProvider(CoreProviderMapper.toAggregationProvider(
                    providerByName.get(credentials.getProviderName())));
            request.setUser(CoreUserMapper.toAggregationUser(user));

            Credentials enrichedCredentials = CoreCredentialsMapper.fromAggregationCredentials(
                    aggregationServiceFactory.getAggregationService().reencryptCredentials(request));

            credentials.setSecretKey(enrichedCredentials.getSecretKey());
            credentialsRepository.save(credentials);
        }
    }

    private static void reencryptAllCredentialsForUser(
            AggregationControllerCommonClient aggregationControllerCommonClient,
            CredentialsRepository credentialsRepository, ImmutableMap<String, Provider> providerByName, User user) {
        List<Credentials> userCredentials = credentialsRepository.findAllByUserId(user.getId());

        for (Credentials credentials : userCredentials) {
            log.info(credentials, "Requesting secret key rotation.");

            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ReencryptionRequest reencryptionRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ReencryptionRequest();
            reencryptionRequest.setCredentials(credentials);
            reencryptionRequest.setProvider(providerByName.get(credentials.getProviderName()));
            reencryptionRequest.setUser(user);

            Credentials enrichedCredentials = aggregationControllerCommonClient.reencryptCredentials(reencryptionRequest);

            credentials.setSecretKey(enrichedCredentials.getSecretKey());
            credentialsRepository.save(credentials);
        }
    }
}
