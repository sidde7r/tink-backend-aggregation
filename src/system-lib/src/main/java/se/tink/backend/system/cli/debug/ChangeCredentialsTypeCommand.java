package se.tink.backend.system.cli.debug;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.debug.credentials.CredentialsTypeChanger;

public class ChangeCredentialsTypeCommand extends ServiceContextCommand<ServiceConfiguration> {
    public ChangeCredentialsTypeCommand() {
        super("change-credentials-type", "Change credentials type");
    }

    private static final String USER_ID_FIELD = "userId";
    private static final String CREDENTIALS_ID_FIELD = "credentialsId";
    private static final String TYPE_TO_CHANGE_TO_FIELD = "changeToType";

    CredentialsRepository credentialsRepository;

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("--userId")
                .dest(USER_ID_FIELD)
                .type(String.class)
                .required(true)
                .help("Id of user to change credentials type for");

        subparser.addArgument("--credentialsId")
                .dest(CREDENTIALS_ID_FIELD)
                .type(String.class)
                .required(true)
                .help("Id of credentials to change credentials type for");

        subparser.addArgument("--changeToType")
                .dest(TYPE_TO_CHANGE_TO_FIELD)
                .type(CredentialsTypes.class)
                .required(true)
                .help("Type to change to");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        String userId = namespace.get(USER_ID_FIELD);
        String credentialsId = namespace.get(CREDENTIALS_ID_FIELD);
        CredentialsTypes typeToChangeTo = namespace.get(TYPE_TO_CHANGE_TO_FIELD);
        validateInput(userId, credentialsId, typeToChangeTo);

        credentialsRepository = injector.getInstance(CredentialsRepository.class);
        Credentials credentials = getAndValidateCredentials(userId, credentialsId, typeToChangeTo);

        new CredentialsTypeChanger(credentialsRepository, injector.getInstance(ProviderRepository.class),
                serviceContext.isProvidersOnAggregation(),
                injector.getInstance(AggregationControllerCommonClient.class))
                .changeCredentialsType(credentials, typeToChangeTo);
    }

    public void validateInput(String userId, String credentialsId, CredentialsTypes typeToChangeTo) {
        Preconditions.checkNotNull(userId, "userId can't be null");
        Preconditions.checkNotNull(credentialsId, "credentialsId can't be null");
        Preconditions.checkNotNull(typeToChangeTo, "typeToChangeTo can't be null");
    }

    public Credentials getAndValidateCredentials(String userId, String credentialsId,
            CredentialsTypes typeToChangeTo) {
        Credentials credentials = credentialsRepository.findOne(credentialsId);
        Preconditions
                .checkNotNull(credentials, String.format("Credentials with id '%s' was not found.", credentialsId));
        Preconditions
                .checkState(Objects.equal(credentials.getUserId(), userId), "Credentials does not belong to user.");
        Preconditions.checkArgument(!Objects.equal(credentials.getType(), typeToChangeTo),
                String.format("Type is already set to the type to change to: %s", typeToChangeTo));

        return credentials;
    }
}
