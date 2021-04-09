package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.danskebankdk;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class DanskeBankDkAccountMigration extends ClusterSafeAgentVersionMigration {

    private static final String CLASS_NAME = "nxgen.dk.openbanking.danskebank.DanskebankV31Agent";
    private static final int EXPECTED_BANK_ID_LENGTH = 10;
    public static final String DUPLICATE = "duplicate";

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
        String newBankId = createNewBankId(account.getBankId());
        log.info(
                "Migrating old bankId: [{}] to the new bankID [{}]",
                account.getBankId(),
                newBankId);
        account.setBankId(newBankId);
    }

    /* After first migration, there can be duplicates in database, which are created by adding a
    suffix "-duplicate-{n}" to the new bankId. These duplicates should be handled/deleted, but
    until it happens, we should handle these cases by extracting the bankId. If there will be an
    account with "-duplicate-{n}" suffix left in database after clean up, it will be automatically
    updated to correct bankId.
    */
    private String createNewBankId(String oldBankId) {
        if (oldBankId.contains(DUPLICATE)) {
            log.info("Duplicated account was found with bankId: {}", oldBankId);
            return StringUtils.left(oldBankId, EXPECTED_BANK_ID_LENGTH);
        }
        return StringUtils.right(oldBankId, EXPECTED_BANK_ID_LENGTH);
    }
}
