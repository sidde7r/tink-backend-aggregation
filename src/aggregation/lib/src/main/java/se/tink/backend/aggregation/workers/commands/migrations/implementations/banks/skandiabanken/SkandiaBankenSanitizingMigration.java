package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.skandiabanken;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SkandiaBankenSanitizingMigration extends ClusterSafeAgentVersionMigration {
    private static final String OLD_AGENT = "banks.SkandiabankenAgent";
    private static final String NEW_AGENT = "nxgen.se.banks.skandiabanken.SkandiaBankenAgent";

    @Override
    public boolean isOldAgent(Provider provider) {
        return provider.getClassName().equals(OLD_AGENT);
    }

    @Override
    public boolean isNewAgent(Provider provider) {
        return provider.getClassName().equals(NEW_AGENT);
    }

    @Override
    public String getNewAgentClassName(Provider oldProvider) {
        return NEW_AGENT;
    }

    @Override
    public boolean isDataMigrated(CredentialsRequest request) {
        return request.getAccounts().stream()
                .noneMatch(acc -> acc.getBankId().contains("-") || acc.getBankId().contains("."));
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().forEach(this::migrateUserAccount);
    }

    private void migrateUserAccount(Account acc) {
        final String bankId = acc.getBankId();
        final String sanitizedBankId;

        switch (acc.getType()) {
            case CHECKING:
            case SAVINGS:
            case OTHER:
                sanitizedBankId = sanitizeTransactional(bankId);
                acc.setBankId(sanitizedBankId);
                break;
            case INVESTMENT:
                sanitizedBankId = sanitizeInvestment(bankId);
                acc.setBankId(sanitizedBankId);
                break;
            default:
        }
    }

    // Is supposed to remove everything that is not a digit
    private String sanitizeInvestment(String string) {
        return string.replaceAll("[^\\d]", "");
    }

    // Old agent unique id: {accoundId}-{accoundId}
    // New agent unique id: {accountId}
    private String sanitizeTransactional(String string) {
        return string.replaceAll("[^\\d]\\w+\\Z", "");
    }
}
