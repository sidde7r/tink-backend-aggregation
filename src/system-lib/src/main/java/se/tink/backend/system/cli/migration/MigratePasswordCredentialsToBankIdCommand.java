package se.tink.backend.system.cli.migration;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.assertj.core.util.Strings;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

/*
 * Migrates users having a PASSWORD provider to its corresponding MOBILE_BANKID provider.
 * It is only applicable for Swedish market.
 * Start command:
 * java -jar -Dexecute=[true|false, i.e. if false run without any changes to datasources]
 *           -DproviderName=[name of PASSWORD provider, e.g. lansforsakringar]
 *           system-service.jar migrate-password-credentials-to-bankid  [config-file]
 *
 * Steps in migration:
 * 1. Extract command line arguments and validate providers exist
 * 2. Fetch all credentials for provider to migrate from and extract distinct user ids from them
 * 3. For each user id:
 *   - a Fetch user
 *   - b Fetch all credentials and filter to keep those applicable for migration i.e. (provider FROM or provider TO)
 *   - c Split the credentials into a map by credential username, i.e. 'credentials.getField(Field.Key.USERNAME)'
 *   - d Create an UserAndCredentials object containing the user and credentials by username
 *   - e Add UserAndCredentials to list 'usersToMigrate' or list 'usersNotAbleToMigrate' after checking if they
 *       can be migrated or not, see below
 *   - f If a valid credentials username is available and credentials doesn't have a valid username update
 *       credentials username
 * 4. Loop list 'usersToMigrate' and migrate users
 * 5. Loop list  and log users not being able to migrate
 * 6. Print some stats:
 *  - number credentials found for the provider to migrate from
 *  - number credentials affected by migration, i.e. credentials from 3 b above
 *  - number credentials migrated
 *  - number credentials deleted
 *
 *  Rules for when a user can be migrated:
 *  a - User has one credentials username AND (that username is a valid swedish SSN OR user has
 *      NationalId set(which will be used))
 *  OR
 *  b - User has several credentials username AND ALL of them have valid swedish SSN
 *
 *  Migration steps:
 *  For each username
 *  1. extract the 'winning' credentials (the first one in the list) and save it as 'credentialsToKeep'
 *  2. filter list of credentials, removing the 'credentialsToKeep' as new list 'credentialsToDelete'
 *  3. if 'credentialsToKeep' is not of type provider TO, update type and name and username field
 *  4. for all credentials in 'credentialsToDelete', use delete controller to delete them
 *
 */
public class MigratePasswordCredentialsToBankIdCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(MigratePasswordCredentialsToBankIdCommand.class);

    private static final String MOBILE_BANKID_SUFFIX = "-bankid";
    static Pattern p = Pattern.compile ("(19|20)[0-9]{10}");


    private CredentialsRepository credentialsRepository;
    private UserRepository userRepository;
    private DeleteController deleteController;
    private ProviderRepository providerRepository;

    private int numCredentialsForProvider;
    private int numCredentialsAffected;
    private int numCredentialsMigrated;
    private int numCredentialsDeleted;

    // command line arguments and their internal representation
    private boolean actuallyExecute = false;
    private Provider providerFrom;
    private Provider providerTo;

    // ctor
    public MigratePasswordCredentialsToBankIdCommand() {
        super("migrate-password-credentials-to-bankid",
                "Migrate PASSWORD credentials to MOBILE_BANKID.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Migrate PASSWORD credentials to MOBILE_BANKID credentials");

        setupDependencies(injector);

        // Input handling, Command line parameters.
        // !dryRun.
        actuallyExecute = Boolean.getBoolean("execute");
        log.info("Migrate PASSWORD credentials to MOBILE_BANKID, execute migration: " + actuallyExecute);

        // password credential provider name
        final String providerName = System.getProperty("providerName");
        providerFrom = findProvider(providerName);
        providerTo = findProvider(providerName + MOBILE_BANKID_SUFFIX);
        log.info(String.format("Migrate users from provider '%s' to provider '%s'",
                providerFrom.getName(), providerTo.getName()));

        // Find all credentials for PASSWORD provider
        List<Credentials> allPasswordCredentials = credentialsRepository
                .findAllByProviderName(providerFrom.getName());

        numCredentialsForProvider += allPasswordCredentials.size();

        // just keep the user ids for the credentials, use a set to exclude any duplicates
        Set<String> allPasswordUserids = allPasswordCredentials.stream()
                .map(Credentials::getUserId)
                .collect(Collectors.toSet());

        List<UserAndCredentials> usersToMigrate = new ArrayList<>();
        List<UserAndCredentials> usersNotAbleToMigrateMissingSSN = new ArrayList<>();
        List<UserAndCredentials> usersNotAbleToMigrateConflictingCredentials = new ArrayList<>();

        // for all users having credentials in need of migration
        for (String userId : allPasswordUserids) {
            // fetch user and all credentials for each user, update credentials username if necessary and possible
            UserAndCredentials userAndCredentials = fetchUserAndCredentials(userId, providerName);

            if (userAndCredentials.allUsernamesValidBankIdUsername()) {
                userAndCredentials.updateCredentialsUsernameIfNecessary();
                usersToMigrate.add(userAndCredentials);
            } else {
                if (userAndCredentials.credentialsByUsername.size() == 1) {
                    usersNotAbleToMigrateMissingSSN.add(userAndCredentials);
                } else {
                    usersNotAbleToMigrateConflictingCredentials.add(userAndCredentials);
                }
            }
        }

        // migrate
        for (UserAndCredentials userAndCredentials : usersToMigrate) {
            userAndCredentials.migrateTo(providerTo);
        }

        // log not migrated
        for (UserAndCredentials userAndCredentials : usersNotAbleToMigrateMissingSSN) {
            userAndCredentials.logNotAbleToMigrateMigrate("Missing SSN");
        }
        for (UserAndCredentials userAndCredentials : usersNotAbleToMigrateConflictingCredentials) {
            userAndCredentials.logNotAbleToMigrateMigrate("Conflicting credentials");
        }

        log.info(String
                .format("\nMigrating from %s to %s Stats:\nNum credentials for provider: %d\nNum credentials affected: %d\nNum credentials migrated: %d\nNum credentials deleted: %d\n",
                        providerFrom.getName(), providerTo.getName(),
                        numCredentialsForProvider, numCredentialsAffected,
                        numCredentialsMigrated, numCredentialsDeleted));
    }

    /*
     * Fetch user and all credentials related to the migration, i.e. any passwords AND bankid credentials for provider.
     * Split credentials by username
     * Return user and credentials
     */
    private UserAndCredentials fetchUserAndCredentials(String userId, String providerName) {
        User user = userRepository.findOne(userId);
        String nationalId = null;

        List<Credentials> allUserCredentials = credentialsRepository.findAllByUserId(userId);
        List<Credentials> userCredentials = allUserCredentials.stream()
                .filter(credentials -> credentials.getProviderName().startsWith(providerName))
                .collect(Collectors.toList());

        if (Strings.isNullOrEmpty(user.getNationalId())) {
            Optional<Credentials> fraudCredentials = allUserCredentials.stream()
                    .filter(credentials -> credentials.getType() == CredentialsTypes.FRAUD)
                    .findFirst();
            if (fraudCredentials.isPresent()) {
                nationalId = fraudCredentials.get().getField(Field.Key.USERNAME);
            }
        } else {
            nationalId = user.getNationalId();
        }

        numCredentialsAffected += userCredentials.size();

        userCredentials.sort(usernameComparator);
        Map<String, List<Credentials>> userCredentialsByUsername = splitByUsername(userCredentials);

        // sort any credentials to have most recently updated as first element
        userCredentialsByUsername.entrySet().stream()
                .forEach(entry -> {
                    entry.getValue().sort(updatedComparator);
                });

        return new UserAndCredentials(user, userCredentialsByUsername, nationalId);
    }

    /*
     * Split all credentials to be migrated for a user by username. A user is only allowed one credentials for the
     * combination of provider - username, i.e. it is not allowed to have two bankid credentials for the same username
     * at the same bank.
     * E.g. provider 'avanze-bankid' and username '201212121218' is only allowed once.
     */
    private Map<String,List<Credentials>> splitByUsername(List<Credentials> sortedCredentials) {
        Map<String, List<Credentials>> credentialsByUsername = new HashMap<>();

        for (Credentials credentials : sortedCredentials) {
            String username = credentials.getField(Field.Key.USERNAME);

            if (credentialsByUsername.containsKey(username)) {
                credentialsByUsername.get(username).add(credentials);
            } else {
                List<Credentials> usernameCredentials = new ArrayList<>();
                usernameCredentials.add(credentials);
                credentialsByUsername.put(username, usernameCredentials);
            }
        }

        return credentialsByUsername;
    }

    private Provider findProvider(String providerName) {
        Provider provider = providerRepository.findByName(providerName);
        if (provider == null) {
            throw new IllegalStateException("No provider found matching: " + providerName);
        }

        return provider;
    }

    // fetch repositories and services needed
    private void setupDependencies(Injector injector) {

        // Repositories
        credentialsRepository = injector.getInstance(CredentialsRepository.class);
        providerRepository = injector.getInstance(ProviderRepository.class);
        userRepository = injector.getInstance(UserRepository.class);

        // Making sure services that should be reachable are reachable. Better to fail early.
        // delete controller, to delete credentials and all connected data
        deleteController = injector.getInstance(DeleteController.class);
    }

    private List<Credentials> getCredentialsToDelete(List<Credentials> credentials,
            List<Credentials> credentialsToKeep) {
        return credentials.stream()
                .filter(allCredentials -> credentialsToKeep.stream().noneMatch(
                        selectedCredentials -> selectedCredentials.getId().equalsIgnoreCase(allCredentials.getId())))
                .collect(Collectors.toList());
    }

    Comparator<Credentials> usernameComparator = new Comparator<Credentials>() {
        @Override
        public int compare(Credentials c1, Credentials c2) {
            if (c1.getField(Field.Key.USERNAME) == null) {
                return 1;
            }
            if (c2.getField(Field.Key.USERNAME) == null) {
                return -1;
            }

            return c1.getField(Field.Key.USERNAME).compareTo(c2.getField(Field.Key.USERNAME));
        }
    };

    Comparator<Credentials> updatedComparator = new Comparator<Credentials>() {
        @Override
        public int compare(Credentials c1, Credentials c2) {
            if (c1.getUpdated() == null) {
                return 1;
            }
            if (c2.getUpdated() == null) {
                return -1;
            }

            return c1.getUpdated().before(c2.getUpdated()) ? 1 : -1;
        }
    };

    // move credentials from PASSWORD provider to MOBILE_BANKID
    // if already MOBILE_BANKID, noop
    // if force update - update even if already MOBILE_BANKID
    private void migrateCredentials(Credentials credentials, Provider providerTo, boolean forceUpdate) {
        logCredentials("KEEP", credentials);
        if (credentials.getType() == providerTo.getCredentialsType() && !forceUpdate) {
            return;
        }

        logCredentials("MIGRATE", credentials);
        numCredentialsMigrated++;
        if (actuallyExecute) {
            executeMigrate(credentials, providerTo);
        }
    }

    // call credentials service for deletion of left over credentials
    private void deleteCredentials(List<Credentials> credentialsToDelete, User user) {
        for (Credentials credentials : credentialsToDelete) {
            logCredentials("DELETE", credentials);
            numCredentialsDeleted++;
            if (actuallyExecute) {
                executeDelete(credentials, user);
            }
        }
    }

    private void logCredentials(String action, Credentials credentials) {
        String username =
                credentials.getField(Field.Key.USERNAME) != null ? credentials.getField(Field.Key.USERNAME) : "N/A";
        String updated = credentials.getUpdated() != null ? credentials.getUpdated().toString() : "N/A";

        log.info(credentials,
                String.format("%s credentials having status %s, with username %s and updated %s",
                        action,
                        credentials.getStatus(),
                        username,
                        updated));
    }

    /**
     * Execute update of credentials.
     * If username for credentials needed update, it should be done while selecting them
     */
    private void executeMigrate(Credentials credentials, Provider targetProvider) {
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setProviderName(targetProvider.getName());
        credentialsRepository.saveAndFlush(credentials);
        log.info(credentials, "MIGRATE - Done");
    }

    /**
     * Execute delete of credentials.
     */
    private void executeDelete(Credentials credentials, User user) {
        try {
            deleteController.deleteCredentials(user, credentials.getId(), false, Optional.empty());
            log.info(credentials, "DELETE - Done");
        } catch (Exception e) {
            log.info(credentials, "DELETE *** FAILED");
            throw e;
        }
    }

    // Class holding a user and all their credentials
    // methods for:
    // find valid username for credentials
    // update username of credentials
    // migrate user
    // log users not possible to migrate
    class UserAndCredentials {
        User user;
        Map<String, List<Credentials>> credentialsByUsername;
        String nationalId;
        boolean credentialsUsernameUpdated;

        public UserAndCredentials(User user,
                Map<String, List<Credentials>> credentialsByUsername,
                String nationalId) {
            this.user = user;
            this.nationalId = nationalId;
            this.credentialsByUsername = credentialsByUsername;
        }

        String getValidUsername() {
            if (credentialsByUsername.size() == 1) {
                String username = getValidCredentialsUsername();
                if (!Strings.isNullOrEmpty(username)) {
                    return username;
                }
                if (!Strings.isNullOrEmpty(nationalId)) {
                    return nationalId;
                }
            }

            return null;
        }

        String getValidCredentialsUsername() {
            if (credentialsByUsername.size() == 1) {
                String username = credentialsByUsername.entrySet().stream().findFirst().get().getKey();
                if (!Strings.isNullOrEmpty(username) && p.matcher(username).matches()) {
                    return username;
                }
            }

            return null;
        }

        boolean hasValidBankIdUsername() {
            return getValidUsername() != null;
        }

        boolean allUsernamesValidBankIdUsername() {
            if (hasValidBankIdUsername()) {
                return true;
            }

            return credentialsByUsername.entrySet().stream()
                    .allMatch(entry -> entry.getKey() != null && p.matcher(entry.getKey()).matches());
        }

        void updateCredentialsUsernameIfNecessary() {
            String validUsername = getValidUsername();
            if (credentialsByUsername.size() == 1 && hasValidBankIdUsername()) {
                List<Credentials> credentialsList = credentialsByUsername.entrySet().stream()
                        .findFirst().get().getValue();

                for (Credentials credentials : credentialsList) {
                    String currentUsername = credentials.getField(Field.Key.USERNAME);
                    if (Strings.isNullOrEmpty(currentUsername) || !p.matcher(currentUsername).matches()) {
                        log.info(credentials, String.format("Credentials username updated from %s to %s",
                                currentUsername, validUsername));
                        credentials.setField(Field.Key.USERNAME, validUsername);
                        credentialsUsernameUpdated = true; // will need update even if bankid
                    }
                }
            }
        }

        void migrateTo(Provider providerTo) {
            credentialsByUsername.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .forEach(credentials -> {
                        Credentials credentialsToKeep = credentials.get(0);
                        List<Credentials> credentialsToDelete = credentials.stream()
                                .filter(c -> !c.getId().equalsIgnoreCase(credentialsToKeep.getId()))
                                .collect(Collectors.toList());

                        migrateCredentials(credentialsToKeep, providerTo, credentialsUsernameUpdated);
                        deleteCredentials(credentialsToDelete, user);
                    });
        }

        void logNotAbleToMigrateMigrate(String reason) {
            credentialsByUsername.forEach((key, value) -> {
                        value.forEach(c -> {
                            log.info(c, String.format("Unable to migrate credentials [%s] with username %s", reason, key));
                        });
                    });
        }
    }
}
