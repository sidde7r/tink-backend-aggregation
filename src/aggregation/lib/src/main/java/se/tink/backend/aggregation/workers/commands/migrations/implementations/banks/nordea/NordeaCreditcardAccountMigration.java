package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class NordeaCreditcardAccountMigration extends ClusterSafeAgentVersionMigration {

    private static final String CREDIT_CARD_FORMAT = "\\d{4}";
    private static final String OBSOLETE_CREDIT_CARD_FORMAT = "\\d{8}";

    private static final Map<AccountTypes, Consumer<Account>> accountMigrators = new HashMap<>();
    private static final Map<AccountTypes, Function<Account, Boolean>> accountMigrationCheckers =
            new HashMap<>();

    static {
        accountMigrators.put(
                AccountTypes.CREDIT_CARD, NordeaCreditcardAccountMigration::migrateCreditCard);
        accountMigrationCheckers.put(
                AccountTypes.CREDIT_CARD, a -> isValidCreditCardId(a.getBankId()));
    }

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
        for (Account account : request.getAccounts()) {
            if (!isMigrated(account)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        for (Account a : request.getAccounts()) {
            migrate(a);
        }
    }

    private static void migrateCreditCard(Account a) {
        if (isValidCreditCardId(a.getBankId())) {
            return;
        }

        String oldId = a.getBankId();
        Pattern pattern = Pattern.compile(OBSOLETE_CREDIT_CARD_FORMAT);
        Matcher matcher = pattern.matcher(oldId);
        if (!isValidCreditCardId(oldId) && matcher.matches()) {
            a.setBankId(oldId.substring(oldId.length() - 4));
            log.info("changed credit card bank_id from \"{}\" to \"{}\"", oldId, a.getBankId());
        }
    }

    private static boolean isValidCreditCardId(String a) {
        return a.trim().matches(CREDIT_CARD_FORMAT);
    }

    private void migrate(Account account) {
        accountMigrators.getOrDefault(account.getType(), x -> {}).accept(account);
    }

    private boolean isMigrated(Account account) {
        return accountMigrationCheckers.getOrDefault(account.getType(), x -> true).apply(account);
    }
}
