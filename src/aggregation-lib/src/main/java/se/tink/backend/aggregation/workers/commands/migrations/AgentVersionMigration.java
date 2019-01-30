package se.tink.backend.aggregation.workers.commands.migrations;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class AgentVersionMigration {

  private ControllerWrapper wrapper;

  /**
   * This method is used to determine which vetrsion of agent for a given provider is used
   * and if it's one using old or new format of `bankId`.
   * @param request
   * @return <code>false</> if the {@param request} was done by a new agent version, <code>true</code> if it uses previous version of agent
   */
  public abstract boolean shouldChangeRequest(CredentialsRequest request);

  /**
   * Performs a check if data associated with these credentials was already migrated.
   * This check is needed as we do not switch to new agent if all of the data was not migrated
   * @param request
   * @return <code>false</> if the {@link CredentialsRequest#getAccounts() account}'s {@link Account#bankId} format corresponds to the new one,
   * <code>true</code> if it's the old format
   */
  public abstract boolean shouldMigrateData(CredentialsRequest request);

  /**
   * Changing the agent that will be used in later processing by using the {@link
   * Provider#setClassName(String) set} method on {@link CredentialsRequest#getProvider() provider
   * in the {@param request}}
   *
   * @param request
   */
  public abstract void changeRequest(CredentialsRequest request);

  /**
   * Implementation of actual migration of {@link Account#bankId} format to the new one. Accounts
   * after the change should be set on the {@link CredentialsRequest request} by the {@link
   * CredentialsRequest#setAccounts(List) setAccounts} method
   *
   * @param request
   */
  public abstract void migrateData(CredentialsRequest request);

  /**
   * Wrapper method that makes sure {@link AgentVersionMigration#migrateAccounts(CredentialsRequest,
   * List)} method is executed and sync with the {@link Account#bankId} value from the {@param
   * request}
   *
   * @param request
   */
  public void updateAccounts(CredentialsRequest request) {
    migrateData(request);
    migrateAccounts(request, request.getAccounts());
  }

  /**
   * This method update the {@param request} with new {@param accounts} and executes the {@link
   * AgentVersionMigration#migrateAccount(Account)} for each account
   *
   * @param request
   * @param accounts
   */
  protected void migrateAccounts(CredentialsRequest request, List<Account> accounts) {
    List<Account> accountList =
        accounts.stream().map(a -> migrateAccount(a)).collect(Collectors.toList());
    request.setAccounts(accountList);
  }

  /**
   * This method sends the update request to system endpoint
   *
   * @param account with new {@link Account#bankId} should be passed
   * @return
   */
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
