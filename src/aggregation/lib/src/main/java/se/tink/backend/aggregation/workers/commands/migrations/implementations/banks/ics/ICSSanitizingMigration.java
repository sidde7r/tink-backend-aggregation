package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.ics;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ICSSanitizingMigration extends ClusterSafeAgentVersionMigration {
    @Override
    public boolean isOldAgent(Provider provider) {
        return true;
    }

    @Override
    public boolean isNewAgent(Provider provider) {
        return true;
    }

    @Override
    public String getNewAgentClassName(Provider oldProvider) {
        return oldProvider.getClassName();
    }

    @Override
    public boolean isDataMigrated(CredentialsRequest request) {
        return request.getAccounts().stream().noneMatch(this::shouldMigrate);
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().forEach(account -> account.setBankId(account.getAccountNumber()));
    }

    private boolean shouldMigrate(Account account) {
        return !account.getBankId().equals(account.getAccountNumber());
    }
}
