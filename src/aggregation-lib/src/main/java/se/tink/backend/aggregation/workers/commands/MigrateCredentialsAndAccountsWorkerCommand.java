package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;

public class MigrateCredentialsAndAccountsWorkerCommand extends AgentWorkerCommand {

  private final ControllerWrapper controllerWrapper;
  private final CredentialsRequest request;
  protected ImmutableList<AgentVersionMigration> migrations =
      ImmutableList.of(
          // Add your migrations here
          );

  public MigrateCredentialsAndAccountsWorkerCommand(
      CredentialsRequest request, ControllerWrapper controllerWrapper) {
    this.request = request;
    this.controllerWrapper = controllerWrapper;
  }

  protected void setMigrations(ImmutableList<AgentVersionMigration> migrations) {
    this.migrations = migrations;
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
