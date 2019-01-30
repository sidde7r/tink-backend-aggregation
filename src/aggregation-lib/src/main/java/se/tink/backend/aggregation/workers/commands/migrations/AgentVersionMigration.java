package se.tink.backend.aggregation.workers.commands.migrations;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class AgentVersionMigration {

  private ControllerWrapper wrapper;

  public abstract boolean shouldChangeRequest(CredentialsRequest request);

  public abstract boolean shouldMigrateData(CredentialsRequest request);

  public abstract void changeRequest(CredentialsRequest request);

  public abstract void migrateData(CredentialsRequest request);

  public void updateAccounts(CredentialsRequest request) {
    migrateData(request);
    migrateAccounts(request, request.getAccounts());
  }

  protected void migrateAccounts(CredentialsRequest request, List<Account> accounts) {
    List<Account> accountList =
        accounts.stream().map(a -> migrateAccount(a)).collect(Collectors.toList());
    request.setAccounts(accountList);
  }

  protected Account migrateAccount(Account account) {
    return getControlWrapper().updateAccountMetaData(account.getId(), account.getBankId());
  }

  public final void setWrapper(ControllerWrapper wrapper) {
    this.wrapper = wrapper;
  }

  private final ControllerWrapper getControlWrapper() {
    return this.wrapper;
  }
}
