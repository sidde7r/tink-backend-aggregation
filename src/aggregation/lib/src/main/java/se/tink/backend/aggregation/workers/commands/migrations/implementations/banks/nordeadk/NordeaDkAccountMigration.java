package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordeadk;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaDkAccountMigration extends ClusterSafeAgentVersionMigration {

    private static final Logger log = LoggerFactory.getLogger(NordeaDkAccountMigration.class);
    private static final String AGENT = "nxgen.dk.banks.nordea.NordeaDkAgent";
    private static final String OBSOLETE_SHORT_ID_FORMAT = "\\d{4}";
    private static final String ACCOUNT_ID_FORMAT = "\\d{10}";
    private static final String CREDIT_CARD_FORMAT = "\\d{4}";
    private static final String OBSOLETE_CREDIT_CARD_FORMAT = "x{12}(\\d{4})";
    private static final String ALL_WHITESPACES = "\\s+";
    private static final String ALL_NON_DIGITS = "[^\\d]+";

    private static final Map<AccountTypes, Consumer<Account>> accountMigrators = new HashMap<>();
    private static final Map<AccountTypes, Function<Account, Boolean>> accountMigrationCheckers =
            new HashMap<>();
    private static final int EXPECTED_ACCOUNT_ID_LENGTH = 10;

    static {
        accountMigrators.put(
                AccountTypes.CHECKING, NordeaDkAccountMigration::migrateCheckingAccount);
        accountMigrators.put(
                AccountTypes.SAVINGS, NordeaDkAccountMigration::migrateCheckingAccount);
        accountMigrators.put(AccountTypes.CREDIT_CARD, NordeaDkAccountMigration::migrateCreditCard);
        accountMigrationCheckers.put(AccountTypes.CHECKING, a -> isValidAccountId(a.getBankId()));
        accountMigrationCheckers.put(AccountTypes.SAVINGS, a -> isValidAccountId(a.getBankId()));
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
        return AGENT;
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

    private boolean isMigrated(Account account) {
        if (account.isExcluded() || account.isClosed()) {
            return true;
        }
        return accountMigrationCheckers.getOrDefault(account.getType(), x -> true).apply(account);
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        for (Account a : request.getAccounts()) {
            migrate(a);
        }
    }

    private void migrate(Account account) {
        if (account.isExcluded() || account.isClosed()) {
            return;
        }
        accountMigrators.getOrDefault(account.getType(), x -> {}).accept(account);
    }

    private static void migrateCreditCard(Account a) {
        if (isValidCreditCardId(a.getBankId())) {
            return;
        }

        String oldId = a.getBankId();
        Pattern pattern = Pattern.compile(OBSOLETE_CREDIT_CARD_FORMAT);
        Matcher matcher = pattern.matcher(oldId);
        if (!isValidCreditCardId(oldId) && matcher.matches()) {
            a.setBankId(matcher.group(1));
            log.info("changed credit card bank_id from \"{}\" to \"{}\"", oldId, a.getBankId());
        }
    }

    private static void migrateCheckingAccount(Account account) {
        if (isValidAccountId(account.getBankId())) {
            return;
        }
        String oldId = account.getBankId();
        String strippedAccountNumber = account.getAccountNumber().replaceAll(ALL_NON_DIGITS, "");
        if (canMigrateAccount(oldId, strippedAccountNumber)) {

            account.setBankId(meaningfulAccountIdCharacters(strippedAccountNumber));
            log.info(
                    "changed bank account bank_id from \"{}\" to \"{}\", which is last 10 digits of account number {}",
                    oldId,
                    account.getBankId(),
                    account.getAccountNumber());
        } else if (oldId.matches(OBSOLETE_SHORT_ID_FORMAT)) {
            log.warn(
                    "account with bankId={} is a candidate for migration but unable to assign a new id, skipping",
                    oldId);
        }
    }

    private static boolean canMigrateAccount(String bankId, String strippedAccountNumber) {
        return bankId.matches(OBSOLETE_SHORT_ID_FORMAT)
                && strippedAccountNumber.length() >= EXPECTED_ACCOUNT_ID_LENGTH;
    }

    private static String meaningfulAccountIdCharacters(String s) {
        return s.substring(s.length() - EXPECTED_ACCOUNT_ID_LENGTH);
    }

    private static boolean isValidAccountId(String a) {
        return a.matches(ACCOUNT_ID_FORMAT);
    }

    private static boolean isValidCreditCardId(String a) {
        return a.replaceAll(ALL_WHITESPACES, "").matches(CREDIT_CARD_FORMAT);
    }
}
