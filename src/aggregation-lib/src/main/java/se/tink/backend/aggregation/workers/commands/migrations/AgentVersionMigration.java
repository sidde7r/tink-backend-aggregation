package se.tink.backend.aggregation.workers.commands.migrations;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public abstract class AgentVersionMigration {
    /**
     * Returns a number between 0 and 1, indicating the ratio of requests that will be migrated.
     * This ratio supersedes the shouldChangeRequest and shouldMigrateData predicates.
     *
     * <p>I.e. even if shouldChangeRequest returns true, it may not migrate the request due to
     * percentToMigrate was below 1 and random change didn't pick this request. If you want all
     * requests to be migrated, this method should return 1.
     *
     * <p>Default is to migrate all.
     */
    public double percentToMigrate() {
        return 1;
    }

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
        se.tink.backend.core.Account accountToSend = CoreAccountMapper.fromAggregation(account);
        return controllerWrapper.updateAccountMetaData(accountToSend.getId(), accountToSend);
    }
}
