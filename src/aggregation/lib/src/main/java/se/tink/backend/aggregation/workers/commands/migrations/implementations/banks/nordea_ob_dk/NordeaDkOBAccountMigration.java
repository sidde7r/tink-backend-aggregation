package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea_ob_dk;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class NordeaDkOBAccountMigration extends ClusterSafeAgentVersionMigration {

    private static final String CLASS_NAME = "nxgen.dk.openbanking.nordea.NordeaDkAgent";
    private static final int EXPECTED_BANK_ID_LENGTH = 10;

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
        return EXPECTED_BANK_ID_LENGTH == account.getBankId().length();
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().forEach(this::migrate);
    }

    private void migrate(Account account) {
        if (isMigrated(account)) {
            return;
        }
        String newBankId = StringUtils.right(account.getBankId(), EXPECTED_BANK_ID_LENGTH);
        log.info(
                "Migrating old bankId: [{}] to the new bankID [{}]",
                account.getBankId(),
                newBankId);
        account.setBankId(newBankId);
    }
}
