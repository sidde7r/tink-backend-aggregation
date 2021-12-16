package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.volvofinans;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class VolvofinansSEUIDMigration extends ClusterSafeAgentVersionMigration {

    private static final String CLASS_NAME = "nxgen.se.banks.volvofinans.VolvoFinansAgent";

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
        return CLASS_NAME;
    }

    @Override
    public boolean isDataMigrated(CredentialsRequest request) {
        return request.getAccounts().stream().allMatch(this::isMigrated);
    }

    private boolean isMigrated(Account account) {
        return account.getBankId().equals(account.getAccountNumber());
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().forEach(this::migrate);
    }

    private void migrate(Account account) {
        if (isMigrated(account)) {
            return;
        }
        log.info(
                "Migrating old bankId: [{}] to the new bankID [{}]",
                account.getBankId(),
                account.getAccountNumber());
        account.setBankId(account.getAccountNumber());
    }
}
