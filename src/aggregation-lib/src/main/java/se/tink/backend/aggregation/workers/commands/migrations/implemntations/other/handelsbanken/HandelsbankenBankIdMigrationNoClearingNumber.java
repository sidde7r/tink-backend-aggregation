package se.tink.backend.aggregation.workers.commands.migrations.implemntations.other.handelsbanken;

import com.google.common.base.Strings;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAgent;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenBankIdMigrationNoClearingNumber extends AgentVersionMigration {
    private static final Pattern CLEARING_NUMBER_PATTERN = Pattern.compile("([0-9]{4})-.*");
    private static final int ACCOUNT_NUMBER_WITHOUT_CLEARING_START_POSITION = 4;
    private Predicate<Account> checkIfAccountIsProperTypeToBeMigrated =
            a -> {
                // only migrate Transactional accounts and CREDIT_CARD of type "Allkort"
                if (a.getType() != AccountTypes.SAVINGS
                        && a.getType() != AccountTypes.CHECKING
                        && a.getType() != AccountTypes.OTHER
                        && a.getType() != AccountTypes.CREDIT_CARD) {
                    return false;
                }
                return true;
            };

    @Override
    public boolean shouldChangeRequest(CredentialsRequest request) {

        if (request.getProvider()
                .getClassName()
                .endsWith(HandelsbankenSEAgent.class.getSimpleName())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldMigrateData(CredentialsRequest request) {
        return !request.getAccounts()
                .stream()
                .filter(checkIfAccountIsProperTypeToBeMigrated)
                .filter(a -> !Strings.isNullOrEmpty(a.getAccountNumber()))
                // Filter out dupicated acounts
                .filter(a -> !a.getBankId().endsWith("-duplicate"))
                // Longer than the account without clearing number
                .filter(a -> a.getBankId().length() > 9)
                //        .filter(a ->
                // CLEARING_NUMBER_PATTERN.matcher(a.getAccountNumber()).matches())
                .collect(Collectors.toList())
                .isEmpty();
    }

    @Override
    public void changeRequest(CredentialsRequest request) {}

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts()
                .stream()
                .filter(checkIfAccountIsProperTypeToBeMigrated)
                .filter(a -> !Strings.isNullOrEmpty(a.getAccountNumber()))
                .filter(a -> CLEARING_NUMBER_PATTERN.matcher(a.getAccountNumber()).matches())
                .forEach(
                        a ->
                                a.setBankId(
                                        a.getAccountNumber()
                                                .replaceAll("[^0-9]", "")
                                                .substring(
                                                        ACCOUNT_NUMBER_WITHOUT_CLEARING_START_POSITION)));
    }
}
