package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaSanitizingMigration extends ClusterSafeAgentVersionMigration {

    private static final String OLD_AGENT = "banks.nordea.NordeaAgent";
    private static final String NEW_AGENT = "nxgen.se.banks.nordea.v30.NordeaSEAgent";

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
                .noneMatch(acc -> acc.getBankId().contains("*") || acc.getBankId().contains(":"));
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().forEach(a -> a.setBankId(sanitize(a.getBankId())));
    }

    private String sanitize(String string) {
        return string.replaceAll("[^A-Za-z0-9]", "");
    }
}
