package se.tink.backend.aggregation.workers.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;

public class MigrateCredentialsAndAccountsWorkerCommand extends AgentWorkerCommand {

  private static final ImmutableList<AgentVersionMigration> CURRENT_MIGRATIONS =
      ImmutableList.of(
          // Add your migrations here
          );

  private final ImmutableList<AgentVersionMigration> migrations;
  private final ControllerWrapper controllerWrapper;
  private final CredentialsRequest request;

  public MigrateCredentialsAndAccountsWorkerCommand(
      CredentialsRequest request, ControllerWrapper controllerWrapper) {
    this.request = request;
    this.controllerWrapper = controllerWrapper;
    this.migrations = CURRENT_MIGRATIONS;
  }

  @VisibleForTesting
  public MigrateCredentialsAndAccountsWorkerCommand(
      CredentialsRequest request,
      ControllerWrapper controllerWrapper,
      ImmutableList<AgentVersionMigration> migrationsOverride) {
    this.request = request;
    this.controllerWrapper = controllerWrapper;
    this.migrations = migrationsOverride;
  }

  @Override
  public AgentWorkerCommandResult execute() throws Exception {

    for (AgentVersionMigration migration : migrations) {

      if (!migration.shouldChangeRequest(request)) {
        // nothing to migrate for this request
        continue;
      }

      migrate(migration);
    }

    return AgentWorkerCommandResult.CONTINUE;
  }

  private void migrate(AgentVersionMigration migration) {
    // Change the Request
    migration.changeRequest(request);

    migration.setWrapper(controllerWrapper);

    if (migration.shouldMigrateData(request)) {
      // Change any data in the database
      migration.migrateData(request);
    }
  }

  @Override
  public void postProcess() throws Exception {
    // Deliberately left empty.
  }
}
