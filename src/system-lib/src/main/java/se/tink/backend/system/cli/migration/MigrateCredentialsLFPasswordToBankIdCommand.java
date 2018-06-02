package se.tink.backend.system.cli.migration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class MigrateCredentialsLFPasswordToBankIdCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(MigrateCredentialsLFPasswordToBankIdCommand.class);

    private static final String LF_PASSWORD_PROVIDER_NAME = "lansforsakringar";
    private static final String LF_MOBILE_BANKID_PROVIDER_NAME = "lansforsakringar-bankid";

    private CredentialsRepository credentialsRepository;
    private UserRepository userRepository;
    private DeleteController deleteController;

    private boolean actuallyExecute = false;

    // ctor
    public MigrateCredentialsLFPasswordToBankIdCommand() {
        super("migrate-lf-credentials-to-bankid", "Migrate LF PASSWORD credentials to MOBILE_BANKID.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Migrate LF PASSWORD credentials to MOBILE_BANKID credentials");

        // Command line parameters.
        // !dryRun.
        actuallyExecute = Boolean.getBoolean("execute");
        log.info("Migrate LF credentials, execute migration: " + actuallyExecute);

        // Repositories
        credentialsRepository = injector.getInstance(CredentialsRepository.class);
        userRepository = injector.getInstance(UserRepository.class);

        // Making sure services that should be reachable are reachable. Better to fail early.
        // delete controller, to delete credentials and all connected data
        deleteController = injector.getInstance(DeleteController.class);

        // Find all credentials for LF PASSWORD provider
        List<Credentials> allPasswordCredentials = credentialsRepository
                .findAllByProviderName(LF_PASSWORD_PROVIDER_NAME);

        // just keep the user ids for the credentials, use a set to exclude any duplicates
        Set<String> allPasswordUserids = allPasswordCredentials.stream()
                .map(Credentials::getUserId)
                .collect(Collectors.toSet());

        // for each userid having LF PASSWORD credentials
        allPasswordUserids.stream()
                .map(userId -> {
                    // fetch all credentials for that user
                    return credentialsRepository.findAllByUserId(userId).stream()
                            // filter so we only save the different type of LF credentials
                            .filter(userCredentials -> userCredentials.getProviderName()
                                    .startsWith(LF_PASSWORD_PROVIDER_NAME))
                            .collect(Collectors.toList());
                })
                .map(userCredentials -> {
                    userCredentials.sort(usernameComparator);
                    return userCredentials;
                })
                // create a list of credentials having the same username, so it is a list(userid) of lists(username)
                .map(this::splitByUsername)
                .flatMap(List::stream)
                // filter any empty lists
                .filter(list -> list.size() > 0)
                // for a users credentials having same username
                .forEach(userCredentials -> {
                    // process each list of credentials by userid/username
                    if (userCredentials.size() == 1) { // if only one credentials it has to be non MOBILE_BANKID
                        migrateCredentials(userCredentials);
                    } else {
                        log.info(userCredentials.get(0), "Multiple LF credentials with same user name");
                        // get credentials to to keep, could be PASSWORD and/or MOBILE_BANKID
                        List<Credentials> credentialsToKeep = getCredentialsToKeep(userCredentials);
                        // get credentials to delete, i.e. any credentials not to keep
                        List<Credentials> credentialsToDelete = getCredentialsToDelete(userCredentials,
                                credentialsToKeep);
                        // migrate credentials, convert PASSWORD to MOBILE_BANKID
                        migrateCredentials(credentialsToKeep);
                        // delete credentials
                        deleteCredentials(credentialsToDelete);
                    }
                });
    }

    private List<List<Credentials>> splitByUsername(List<Credentials> sortedCredentials) {
        List<List<Credentials>> credentialsByUsername = new ArrayList<>();

        String currentUsername = "";
        List<Credentials> credentials = new ArrayList<>();
        for (Credentials userCredential : sortedCredentials) {
            if (currentUsername.isEmpty() || currentUsername
                    .equalsIgnoreCase(userCredential.getField(Field.Key.USERNAME))) {
                currentUsername = userCredential.getField(Field.Key.USERNAME) != null ? userCredential.getField(Field.Key.USERNAME) : "";
                credentials.add(userCredential);
            } else {
                credentialsByUsername.add(credentials);
                credentials = new ArrayList<>();
                credentials.add(userCredential);
                currentUsername = userCredential.getField(Field.Key.USERNAME) != null ? userCredential.getField(Field.Key.USERNAME) : "";
            }
        }
        credentialsByUsername.add(credentials);

        return credentialsByUsername;
    }

    private List<Credentials> getCredentialsToKeep(List<Credentials> userCredentials) {
        // all credentials are sorted by username by now
        // sort by updated
        userCredentials.sort(updatedComparator);

        return ImmutableList.of(userCredentials.get(0));
    }

    private List<Credentials> getCredentialsToDelete(List<Credentials> credentials,
            List<Credentials> credentialsToKeep) {
        return credentials.stream()
                .filter(lfCredentials -> credentialsToKeep.stream().noneMatch(
                        selectedCredentials -> selectedCredentials.getId().equalsIgnoreCase(lfCredentials.getId())))
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
    private void migrateCredentials(List<Credentials> credentialsToKeep) {
        List<Credentials> credentialsToMigrate = filterCredentialsToMigrate(credentialsToKeep);

        for (Credentials credentials : credentialsToKeep) {
            logCredentials("KEEP", credentials);
        }
        for (Credentials credentials : credentialsToMigrate) {
            logCredentials("MIGRATE", credentials);
            if (actuallyExecute) {
                executeMigrate(credentials);
            }
        }
    }

    private List<Credentials> filterCredentialsToMigrate(List<Credentials> userLfCredentials) {
        return userLfCredentials.stream()
                .filter(credentials -> LF_PASSWORD_PROVIDER_NAME.equalsIgnoreCase(credentials.getProviderName()))
                .collect(Collectors.toList());
    }

    // call credentials service for deletion of left over credentials
    private void deleteCredentials(List<Credentials> credentialsToDelete) {
        for (Credentials credentials : credentialsToDelete) {
            logCredentials("DELETE", credentials);
            if (actuallyExecute) {
                executeDelete(credentials);
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
     */
    private void executeMigrate(Credentials credentials) {
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setProviderName(LF_MOBILE_BANKID_PROVIDER_NAME);
        credentialsRepository.saveAndFlush(credentials);
        log.info(credentials, "MIGRATE - Done");
    }

    /**
     * Execute delete of credentials.
     */
    private void executeDelete(Credentials credentials) {
        User user = userRepository.findOne(credentials.getUserId());

        try {
            deleteController.deleteCredentials(user, credentials.getId(), false, Optional.empty());
            log.info(credentials, "DELETE - Done");
        } catch (Exception e) {
            log.info(credentials, "DELETE *** FAILED");
            throw e;
        }
    }
}
