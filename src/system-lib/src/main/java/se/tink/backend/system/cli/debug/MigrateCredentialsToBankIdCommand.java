package se.tink.backend.system.cli.debug;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.system.cli.ServiceContextCommand;

public class MigrateCredentialsToBankIdCommand extends ServiceContextCommand<ServiceConfiguration> {
    public MigrateCredentialsToBankIdCommand() {
        super("migrate-credentials-to-bankid",
                "Migrates a specific credentials to BankID.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input handling
        final String userId = System.getProperty("userId");
        System.out.println("userId to search for is: " + userId);

        final String credentialsId = System.getProperty("credentialsId");
        System.out.println("credentialsId to search for is: " + credentialsId);

        // Input validation

        Preconditions.checkNotNull(userId, "userId must not be null.");
        Preconditions.checkNotNull(credentialsId, "credentialsId must not be null.");

        // Make the actual change.

        CredentialsRepository credentialsRepository = injector.getInstance(CredentialsRepository.class);

        Credentials credentials = credentialsRepository.findOne(credentialsId);
        Preconditions
                .checkNotNull(credentials, String.format("credentials with id '%s' was not found.", credentialsId));
        Preconditions
                .checkState(Objects.equal(credentials.getUserId(), userId), "Credentials does not belong to user.");
        Preconditions.checkArgument(!Objects.equal(credentials.getType(), CredentialsTypes.MOBILE_BANKID),
                "Type already for credentials is already MOBILE_BANKID");
        Preconditions.checkArgument(!credentials.getProviderName().endsWith("-bankid"),
                "Provider name of credentials is already *-bankid");

        String newProviderName = credentials.getProviderName() + "-bankid";

        AggregationControllerCommonClient aggregationControllerClient = injector.getInstance(
                AggregationControllerCommonClient.class);
        ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);

        Provider provider;
        if (serviceContext.isProvidersOnAggregation()) {
            provider = aggregationControllerClient.getProviderByName(newProviderName);
        } else {
            provider = providerRepository.findByName(newProviderName);
        }

        Preconditions
                .checkNotNull(provider, String.format("Provider with name '%s' is not supported.", newProviderName));

        credentials.setProviderName(newProviderName);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        credentialsRepository.save(credentials);

        System.out.println(
                String.format("Credentials with id '%s' now changed to '%s'.", credentialsId, newProviderName));
    }
}
