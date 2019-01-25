package se.tink.backend.aggregation.workers.commands.migrations.implemntations.other.handelsbanken;

import com.google.common.base.Strings;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAgent;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;

public class HandelsbankenBankIdMigrationNoClearingNumber extends AgentVersionMigration {
    public static final String HANDELSBANKEN_PROVIDER_NAME = "handelsbanken-bankid";
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

        if (HANDELSBANKEN_PROVIDER_NAME.equals(request.getProvider().getName())
                && !request.getProvider()
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
                .filter(a -> CLEARING_NUMBER_PATTERN.matcher(a.getAccountNumber()).matches())
                .collect(Collectors.toList())
                .isEmpty();
    }

    @Override
    public void changeRequest(CredentialsRequest request) {
        //        request.getProvider().setClassName(HandelsbankenSEAgent.class.getSimpleName());
        request.getProvider().setClassName("nxgen.se.banks.handelsbanken.HandelsbankenSEAgent");
    }

    @Override
    public void migrateData(ControllerWrapper controllerWrapper, CredentialsRequest request) {
        List<Account> collect =
                request.getAccounts()
                        .stream()
                        .filter(checkIfAccountIsProperTypeToBeMigrated)
                        .filter(a -> !Strings.isNullOrEmpty(a.getAccountNumber()))
                        .filter(
                                a ->
                                        CLEARING_NUMBER_PATTERN
                                                .matcher(a.getAccountNumber())
                                                .matches())
                        .map(
                                a ->
                                        controllerWrapper.updateAccountMetaData(
                                                a.getId(),
                                                a.getAccountNumber()
                                                        .replaceAll("[^0-9]", "")
                                                        .substring(
                                                                ACCOUNT_NUMBER_WITHOUT_CLEARING_START_POSITION)))
                        .collect(Collectors.toList());
        request.setAccounts(collect);
    }
}
