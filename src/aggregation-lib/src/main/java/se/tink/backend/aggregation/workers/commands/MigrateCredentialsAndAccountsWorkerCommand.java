package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MigrateCredentialsAndAccountsWorkerCommand extends AgentWorkerCommand {

  private final ControllerWrapper controllerWrapper;
  private final CredentialsRequest request;
  protected ImmutableMap<String, AgentVersionMigration> migrations =
      ImmutableMap.of(
          // Add your migrations here
          );

  public MigrateCredentialsAndAccountsWorkerCommand(
      CredentialsRequest request, ControllerWrapper controllerWrapper) {
    this.request = request;
    this.controllerWrapper = controllerWrapper;
  }

  protected void setMigrations(ImmutableMap<String, AgentVersionMigration> migrations) {
    this.migrations = migrations;
  }

  @Override
  public AgentWorkerCommandResult execute() throws Exception {

    migrations
        .entrySet()
        .stream()
        .filter(e -> e.getKey().equals(request.getProvider().getName()))
        .map(e -> e.getValue())
        .filter(m -> m.shouldChangeRequest(request))
        .forEach(m -> migrate(m));

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
