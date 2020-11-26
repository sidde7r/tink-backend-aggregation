package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandles;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.icabanken.IcaBankenSanitizingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.ics.ICSSanitizingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.jyskebank.JyskebankSanitizingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea.NordeaSanitizingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordeadk.NordeaDkAccountMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.skandiabanken.SkandiaBankenSanitizingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.brokers.avanza.AvanzaStripClearingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.creditcards.norwegian.NorwegianSanitizingMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.entercard.EnterCardAccountIdMigration;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.sebkort.SebKortSanitizeUniqueIdentifierMgration;
import se.tink.backend.aggregation.workers.commands.migrations.implemntations.other.handelsbanken.HandelsbankenBankIdMigrationNoClearingNumber;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MigrateCredentialsAndAccountsWorkerCommand extends AgentWorkerCommand {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ControllerWrapper controllerWrapper;
    private final CredentialsRequest request;
    private final ClientInfo clientInfo;
    protected ImmutableMap<String, AgentVersionMigration> migrations =
            new ImmutableMap.Builder<String, AgentVersionMigration>()
                    // Add your migrations here
                    .put("handelsbanken-bankid", new HandelsbankenBankIdMigrationNoClearingNumber())

                    // SEB Kort migrations
                    .put(
                            "chevroletmastercard-bankid",
                            new SebKortSanitizeUniqueIdentifierMgration())
                    .put("choicemastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put(
                            "djurgardskortetmastercard-bankid",
                            new SebKortSanitizeUniqueIdentifierMgration())
                    .put("eurocard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put("finnairmastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put("jetmastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put(
                            "nknyckelnmastercard-bankid",
                            new SebKortSanitizeUniqueIdentifierMgration())
                    .put("opelmastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put("saabmastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put(
                            "saseurobonusmastercard-bankid",
                            new SebKortSanitizeUniqueIdentifierMgration())
                    .put(
                            "sebwalletmastercard-bankid",
                            new SebKortSanitizeUniqueIdentifierMgration())
                    .put("sjpriomastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put("statoilmastercard-bankid", new SebKortSanitizeUniqueIdentifierMgration())
                    .put("skandiabanken-bankid", new SkandiaBankenSanitizingMigration())
                    .put("skandiabanken-ssn-bankid", new SkandiaBankenSanitizingMigration())
                    .put("avanza-bankid", new AvanzaStripClearingMigration())
                    .put("icabanken-bankid", new IcaBankenSanitizingMigration())
                    .put("nl-ics-oauth2", new ICSSanitizingMigration())
                    .put("nordea-bankid", new NordeaSanitizingMigration())
                    .put("norwegian-bankid", new NorwegianSanitizingMigration())
                    .put("dk-jyskebank-codecard", new JyskebankSanitizingMigration())
                    .put("dk-nordea-nemid", new NordeaDkAccountMigration())
                    .put("coop-bankid", new EnterCardAccountIdMigration())
                    .put("remembermastercard-bankid", new EnterCardAccountIdMigration())
                    .put("moregolfmastercard-bankid", new EnterCardAccountIdMigration())
                    .build();

    public MigrateCredentialsAndAccountsWorkerCommand(
            CredentialsRequest request,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {
        this.request = request;
        this.controllerWrapper = controllerWrapper;
        this.clientInfo = clientInfo;
    }

    @VisibleForTesting
    protected void setMigrations(ImmutableMap<String, AgentVersionMigration> migrations) {
        this.migrations = migrations;
    }

    /**
     * This method execuds a command to migrate value of `bankId` to a new format. It checks the
     * provder name and looks for it in the {@link
     * MigrateCredentialsAndAccountsWorkerCommand#migrations map}.
     *
     * @return {@link AgentWorkerCommandResult#CONTINUE status} after successful execution
     * @throws Exception
     */
    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {

        migrations.entrySet().stream()
                .filter(e -> e.getKey().equals(request.getProvider().getName()))
                .map(e -> e.getValue())
                .filter(m -> m.shouldChangeRequest(request))
                .forEach(m -> migrate(m));

        return AgentWorkerCommandResult.CONTINUE;
    }

    /**
     * @param migration is an instance of {@link AgentVersionMigration} that implements the logics
     *     for the new `bankId` format as well as the validation if a migration should be executed.
     */
    private void migrate(AgentVersionMigration migration) {
        logger.debug(String.format("Migrating request for %s", request.getProvider().getName()));

        migration.setWrapper(controllerWrapper);
        migration.setClientIfo(clientInfo);

        // Change the Request
        migration.changeRequest(request);

        if (migration.shouldMigrateData(request)) {
            // Change any data in the database
            migration.updateAccounts(request);
        }
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }
}
