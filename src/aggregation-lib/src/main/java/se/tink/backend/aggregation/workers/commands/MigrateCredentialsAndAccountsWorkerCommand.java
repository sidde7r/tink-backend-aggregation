package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.VisibleForTesting;
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

  @VisibleForTesting
  protected void setMigrations(ImmutableMap<String, AgentVersionMigration> migrations) {
    this.migrations = migrations;
  }

  /**
   * This method execuds a command to migrate value of `bankId` to a new format. It checks the
   * provder name and looks for it in the {@link
   * MigrateCredentialsAndAccountsWorkerCommand#migrations map}.
   *
   * @return {@link AgentWorkerCommandResult#CONTINUE status} after successful execution
   * @throws Exception
   */
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

  /**
   * @param migration is an instance of {@link AgentVersionMigration} that implements the logics for
   *     the new `bankId` format as well as the validation if a migration should be executed.
   */
  private void migrate(AgentVersionMigration migration) {
    // Change the Request
    migration.changeRequest(request);

    migration.setWrapper(controllerWrapper);

    if (migration.shouldMigrateData(request)) {
      // Change any data in the database
      migration.updateAccounts(request);
    }
  }

  @Override
  public void postProcess() throws Exception {
    // Deliberately left empty.
  }
}
