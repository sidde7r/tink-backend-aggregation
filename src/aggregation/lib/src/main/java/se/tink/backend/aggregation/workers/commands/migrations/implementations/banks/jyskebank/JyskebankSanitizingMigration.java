package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.jyskebank;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class JyskebankSanitizingMigration extends ClusterSafeAgentVersionMigration {

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
        return request.getAccounts().stream()
                .filter(account -> account.getIdentifier(AccountIdentifier.Type.IBAN) != null)
                .anyMatch(
                        account ->
                                account.getBankId()
                                        .equals(
                                                account.getIdentifier(
                                                        AccountIdentifier.Type.IBAN)));
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts().stream()
                .filter(account -> account.getIdentifier(AccountIdentifier.Type.IBAN) != null)
                .forEach(
                        account ->
                                account.setBankId(
                                        account.getIdentifier(AccountIdentifier.Type.IBAN)
                                                .getIdentifier()));
    }
}
