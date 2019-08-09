package se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.bankdata;

import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankdataAccountIdMigration extends AgentVersionMigration {

  @Override
  public boolean shouldChangeRequest(CredentialsRequest request) {
    // bank ID should be the IBAN  (account number), otherwise it's using the old format
    return request.getAccounts().stream().anyMatch( acc -> !acc.getBankId().equals(acc.getAccountNumber()));
  }

  @Override
  public boolean shouldMigrateData(CredentialsRequest request) {
    return true;
  }

  @Override
  public void changeRequest(CredentialsRequest request) {
    // no need to change the agent
  }

  @Override
  public void migrateData(CredentialsRequest request) {
    request.getAccounts().forEach(account -> account.setBankId(account.getAccountNumber()));
  }

}
