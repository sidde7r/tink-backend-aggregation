package se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.entercard;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EnterCardAccountIdMigration extends AgentVersionMigration {

    @Override
    public boolean shouldChangeRequest(CredentialsRequest request) {
        return request.getAccounts().stream().allMatch(account -> shouldChangeBankId(account));
    }

    @Override
    public boolean shouldMigrateData(CredentialsRequest request) {
        return request.getAccounts().stream().allMatch(account -> shouldChangeBankId(account));
    }

    @Override
    public void changeRequest(CredentialsRequest request) {}

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts()
                .forEach(
                        account ->
                                account.setBankId(
                                        account.getIdentifier(
                                                        AccountIdentifierType.PAYMENT_CARD_NUMBER)
                                                .getIdentifier()));
    }

    private boolean shouldChangeBankId(Account account) {
        if (!account.getIdentifiers().isEmpty()) {
            return !account.getIdentifier(AccountIdentifierType.PAYMENT_CARD_NUMBER)
                    .getIdentifier()
                    .equals(account.getBankId());
        }
        return false;
    }
}
