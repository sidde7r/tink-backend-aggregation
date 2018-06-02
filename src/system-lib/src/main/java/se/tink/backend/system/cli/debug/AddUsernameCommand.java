package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class AddUsernameCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(AddUsernameCommand.class);

    public AddUsernameCommand() {
        super("add-username", "Add username field to user's credentials");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Handle input

        final String providerName = System.getProperty("providerName");
        Preconditions.checkNotNull(providerName, "providerName must not be null.");
        log.info(String.format("providerName to search for is: %s", providerName));

        // Get all of the provider's credentials

        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);

        List<Credentials> providerCredentials = credentialsRepository.findAllByProviderName(providerName);
        Preconditions.checkArgument(providerCredentials != null && !providerCredentials.isEmpty(),
                "Provider credentials was not found");
        log.info(String.format("Number of %s credentials: %d", providerName, providerCredentials.size()));

        // Find credentials with no username field

        List<Credentials> credentialsWithoutUsername = getCredentialsWithoutUsername(providerCredentials);
        log.info(String.format(
                "Number of %s credentials without username: %d", providerName, credentialsWithoutUsername.size()));

        int numberOfUpdatedCredentials = 0;

        // First check for credit safe provider, it's preferable to use the username of this provider.
        // With no credit safe provider found, check user's other credentials.

        for (Credentials credentials : credentialsWithoutUsername) {
            if (updateWithCreditsafeUserName(credentialsRepository, credentials) ||
                    updateWithOtherCredentialsUserName(credentialsRepository, credentials)) {
                numberOfUpdatedCredentials++;
            }
        }

        log.info(String.format("Number of credentials updated with username: %d", numberOfUpdatedCredentials));
        log.info(String.format("Number of credentials that were not updated: %d",
                credentialsWithoutUsername.size() - numberOfUpdatedCredentials));
        log.info("Done running job");
    }

    private boolean updateWithCreditsafeUserName(CredentialsRepository credentialsRepository,
            Credentials credentials) {
        List<Credentials> creditSafeCredentials = credentialsRepository
                .findAllByUserIdAndProviderName(credentials.getUserId(), "creditsafe");

        if (!creditSafeCredentials.isEmpty()) {
            log.info("Credit safe provider found");
            credentials.setField(
                    Field.Key.USERNAME,
                    creditSafeCredentials.get(0).getField(Field.Key.USERNAME));
            credentialsRepository.save(credentials);
            return true;
        }

        return false;
    }

    private boolean updateWithOtherCredentialsUserName(CredentialsRepository credentialsRepository,
            Credentials credentials) {
        List<Credentials> validSSNCredentials = credentialsRepository
                .findAllByUserId(credentials.getUserId()).stream()
                .filter(this::hasValidSsn)
                .collect(Collectors.toList());

        if (validSSNCredentials.isEmpty()) {
            return false;
        }

        // Only update credentials if all user names match

        String firstUserNameInList = validSSNCredentials.get(0).getField(Field.Key.USERNAME);

        boolean allUserNamesMatch = validSSNCredentials.stream()
                .map(cred -> cred.getField(Field.Key.USERNAME))
                .allMatch(firstUserNameInList::equals);

        if (allUserNamesMatch) {
            credentials.setField(Field.Key.USERNAME, firstUserNameInList);
            credentialsRepository.save(credentials);
            return true;
        }

        return false;
    }

    private boolean hasValidSsn(Credentials credentials) {
        SocialSecurityNumber.Sweden ssn =
                new SocialSecurityNumber.Sweden(credentials.getField(Field.Key.USERNAME));
        return ssn.isValid();
    }

    private List<Credentials> getCredentialsWithoutUsername(List<Credentials> providerCredentials) {
    return providerCredentials.stream()
            .filter(credentials -> Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME)))
            .collect(Collectors.toList());
    }
}
