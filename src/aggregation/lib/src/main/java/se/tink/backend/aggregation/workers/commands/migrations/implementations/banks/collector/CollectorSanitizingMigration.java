package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.collector;

import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CollectorSanitizingMigration extends ClusterSafeAgentVersionMigration {

    private static final String OLD_AGENT = "banks.se.collector.CollectorAgent";
    private static final String NEW_AGENT = "nxgen.se.banks.collector.CollectorAgent";
    private static final String NON_ALPHANUMERIC_REGEX = "[^A-Za-z0-9]";

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
                .noneMatch(
                        account ->
                                Pattern.compile(NON_ALPHANUMERIC_REGEX)
                                        .matcher(account.getBankId())
                                        .find());
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts()
                .forEach(
                        account ->
                                account.setBankId(
                                        (account.getBankId()
                                                .replaceAll(NON_ALPHANUMERIC_REGEX, ""))));
    }
}
