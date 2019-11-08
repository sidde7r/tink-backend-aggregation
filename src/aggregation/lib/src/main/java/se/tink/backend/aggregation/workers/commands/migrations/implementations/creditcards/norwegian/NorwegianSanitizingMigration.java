package se.tink.backend.aggregation.workers.commands.migrations.implementations.creditcards.norwegian;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NorwegianSanitizingMigration extends ClusterSafeAgentVersionMigration {

    private static final String OLD_AGENT = "banks.norwegian.NorwegianAgent";
    private static final String NEW_AGENT = "nxgen.se.creditcards.norwegian.NorwegianAgent";

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
        return request.getAccounts().stream().noneMatch(acc -> acc.getBankId().contains("_"));
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().forEach(a -> a.setBankId(sanitize(a.getBankId())));
    }

    private String sanitize(String uniqueIdentifier) {
        return uniqueIdentifier.replaceAll("[^\\dA-Za-z]", "");
    }
}
