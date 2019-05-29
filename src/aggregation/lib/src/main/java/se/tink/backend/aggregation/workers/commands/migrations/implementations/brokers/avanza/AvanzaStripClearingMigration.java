package se.tink.backend.aggregation.workers.commands.migrations.implementations.brokers.avanza;

import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AvanzaStripClearingMigration extends ClusterSafeAgentVersionMigration {

    private static final String LEGACY_AVANZA_AGENT = "brokers.avanza.AvanzaV2Agent";
    private static final String NXGEN_AVANZA_AGENT = "nxgen.se.brokers.avanza.AvanzaAgent";

    private static final Pattern ACCOUNT_WITH_CLEARING = Pattern.compile("\\d+-\\d+");

    @Override
    public boolean isOldAgent(Provider provider) {
        return provider.getClassName().equals(LEGACY_AVANZA_AGENT);
    }

    @Override
    public boolean isNewAgent(Provider provider) {
        return provider.getClassName().equals(NXGEN_AVANZA_AGENT);
    }

    @Override
    public String getNewAgentClassName(Provider oldProvider) {
        return NXGEN_AVANZA_AGENT;
    }

    @Override
    public boolean isDataMigrated(CredentialsRequest request) {
        return request.getAccounts().stream()
                .noneMatch(a -> ACCOUNT_WITH_CLEARING.matcher(a.getBankId()).matches());
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().stream()
                .filter(a -> ACCOUNT_WITH_CLEARING.matcher(a.getBankId()).matches())
                .forEach(a -> a.setBankId(removeClearing(a.getBankId())));
    }

    private String removeClearing(String uniqueIdentifier) {
        final String[] parts = uniqueIdentifier.split("-", 2);
        return parts[1];
    }
}
