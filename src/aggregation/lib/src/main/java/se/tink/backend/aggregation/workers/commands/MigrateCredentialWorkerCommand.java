package se.tink.backend.aggregation.workers.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration.MigrationResult;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.MigrationFailedException;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.MigrationSkippedException;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.implementations.avanza.Sanitizing_v1_v2;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MigrateCredentialWorkerCommand extends AgentWorkerCommand {

    private static final Logger logger =
            LoggerFactory.getLogger(MigrateCredentialWorkerCommand.class);
    private static final String MIGRATION_RESULT =
            "{} credential {} (v{}) from v{} to v{}, target v{}";

    private final CredentialsRequest request;
    private final ClientInfo clientInfo;
    private final int targetVersion;
    private AgentWorkerCommandContext context;
    private final ControllerWrapper controllerWrapper;
    private Map<Account, String> migrationResult;

    public MigrateCredentialWorkerCommand(
            CredentialsRequest request,
            ClientInfo clientInfo,
            int targetVersion,
            AgentWorkerCommandContext context,
            ControllerWrapper controllerWrapper) {
        this.request = request;
        this.clientInfo = clientInfo;
        this.targetVersion = targetVersion;
        this.context = context;
        this.controllerWrapper = controllerWrapper;
    }

    private ImmutableMap<String, List<DataVersionMigration>> migrations =
            new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                    .put(
                            "avanza-bankid",
                            Lists.newArrayList(new Sanitizing_v1_v2(), new Sanitizing_v1_v2()))
                    .build();

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        return successfulMigration()
                ? AgentWorkerCommandResult.CONTINUE
                : AgentWorkerCommandResult.ABORT;
    }

    @VisibleForTesting
    void setMigrations(ImmutableMap<String, List<DataVersionMigration>> migrations) {
        this.migrations = migrations;
    }

    boolean successfulMigration() {
        return migrations.entrySet().stream()
                .filter(migration -> migration.getKey().equals(request.getProvider().getName()))
                .map(Entry::getValue)
                .flatMap(List::stream)
                .map(this::doVersionMigrations)
                .noneMatch(result -> result == MigrationResult.ABORT);
    }

    MigrationResult doVersionMigrations(DataVersionMigration migration) {
        final Credentials credentials = request.getCredentials();
        final int from = migration.getMigrationFromVersion();
        final int to = migration.getMigrationToVersion();

        final MigrationResult result =
                (from < targetVersion)
                        ? migrateWithResult(migration, request, clientInfo)
                        : MigrationResult.TOO_HIGH;

        if (result == MigrationResult.MIGRATED) {
            if (!isValidMigratedCredentials(request)) {
                logger.info(
                        MIGRATION_RESULT,
                        "FAILED due to collision:",
                        credentials.getId(),
                        credentials.getDataVersion(),
                        from,
                        to,
                        targetVersion);

                return MigrationResult.ABORT;
            }

            credentials.setDataVersion(to);
        }

        logger.info(
                MIGRATION_RESULT,
                result,
                credentials.getId(),
                credentials.getDataVersion(),
                from,
                to,
                targetVersion);
        return result;
    }

    private MigrationResult migrateWithResult(
            DataVersionMigration migration, CredentialsRequest request, ClientInfo clientInfo) {
        return Try.of(
                        () -> {
                            migrationResult = migration.migrate(request, clientInfo);
                            return MigrationResult.MIGRATED;
                        })
                .recover(MigrationSkippedException.class, MigrationResult.SKIP)
                .recover(MigrationFailedException.class, MigrationResult.ABORT)
                .get();
    }

    static boolean isValidMigratedCredentials(CredentialsRequest request) {
        Set<String> distinctIdentifiers = new HashSet<>();

        // Find duplicated identifiers, if none are found we have a valid migration
        return !request.getAccounts().stream()
                .map(Account::getBankId)
                .filter(identifier -> !distinctIdentifiers.add(identifier))
                .peek(
                        identifier ->
                                logger.error(
                                        "UniqueIdentifier {} occurs more than once.", identifier))
                .findFirst()
                .isPresent();
    }

    @Override
    public void postProcess() throws Exception {
        if (migrationResult != null) {
            context.updateCredentialsExcludingSensitiveInformation(
                    request.getCredentials(), true, true);

            HashMap.ofAll(migrationResult)
                    .mapKeys(Account::getId)
                    .forEach(controllerWrapper::updateAccountMetaData);
        }
    }
}
