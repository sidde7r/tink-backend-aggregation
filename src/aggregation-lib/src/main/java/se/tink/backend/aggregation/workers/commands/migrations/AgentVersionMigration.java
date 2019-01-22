package se.tink.backend.aggregation.workers.commands.migrations;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public abstract class AgentVersionMigration {

    public abstract boolean shouldChangeRequest(CredentialsRequest request);

    public abstract boolean shouldMigrateData(CredentialsRequest request);

    public abstract void changeRequest(CredentialsRequest request);

    public abstract void migrateData(
            final ControllerWrapper controllerWrapper, CredentialsRequest request);

    protected void migrateAccounts(
            final ControllerWrapper controllerWrapper,
            CredentialsRequest request,
            List<se.tink.backend.agents.rpc.Account> accounts) {
        for (Account account : accounts) {
            int index = accounts.indexOf(account);
            Account newAccount = migrateAccount(controllerWrapper, account);
            request.getAccounts().remove(index);
            request.getAccounts().add(index, newAccount);
        }
    }

    protected Account migrateAccount(final ControllerWrapper controllerWrapper, Account account) {
        return controllerWrapper.updateAccountMetaData(account.getId(), account.getBankId());
    }
}
