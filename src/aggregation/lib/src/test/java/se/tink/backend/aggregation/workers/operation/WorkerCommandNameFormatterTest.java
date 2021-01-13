package se.tink.backend.aggregation.workers.operation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.workers.commands.AbnAmroSpecificCase;
import se.tink.backend.aggregation.workers.commands.AccountWhitelistRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ClearSensitiveInformationCommand;
import se.tink.backend.aggregation.workers.commands.CreateAgentConfigurationControllerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateBeneficiaryAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateLogMaskerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.EncryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ExpireSessionAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.MigrateCredentialsAndAccountsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.Psd2PaymentAccountRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshCommandChainEventTriggerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderTransferMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RequestUserOptInAccountsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsHoldersToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendDataForProcessingAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendFetchedDataToDataAvailabilityTrackerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.UpdateCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;

public class WorkerCommandNameFormatterTest {

    @Test
    public void shouldGenerateHumanReadableCommandNames() {
        assertEquals("abn-amro-specific-case", get(AbnAmroSpecificCase.class));
        assertEquals(
                "account-whitelist-restriction",
                get(AccountWhitelistRestrictionWorkerCommand.class));
        assertEquals("circuit-breaker", get(CircuitBreakerAgentWorkerCommand.class));
        assertEquals("clear-sensitive-information", get(ClearSensitiveInformationCommand.class));
        assertEquals(
                "create-agent-configuration-controller",
                get(CreateAgentConfigurationControllerWorkerCommand.class));
        assertEquals("create-beneficiary", get(CreateBeneficiaryAgentWorkerCommand.class));
        assertEquals("create-log-masker", get(CreateLogMaskerWorkerCommand.class));
        assertEquals("debug", get(DebugAgentWorkerCommand.class));
        assertEquals("decrypt-credentials", get(DecryptCredentialsWorkerCommand.class));
        assertEquals("encrypt-credentials", get(EncryptCredentialsWorkerCommand.class));
        assertEquals("expire-session", get(ExpireSessionAgentWorkerCommand.class));
        assertEquals("instantiate", get(InstantiateAgentWorkerCommand.class));
        assertEquals("lock", get(LockAgentWorkerCommand.class));
        assertEquals("login", get(LoginAgentWorkerCommand.class));
        assertEquals(
                "migrate-credentials-and-accounts",
                get(MigrateCredentialsAndAccountsWorkerCommand.class));
        assertEquals(
                "psd2payment-account-restriction",
                get(Psd2PaymentAccountRestrictionWorkerCommand.class));
        assertEquals(
                "refresh-chain-event-trigger", get(RefreshCommandChainEventTriggerCommand.class));
        assertEquals("refresh-item", get(RefreshItemAgentWorkerCommand.class));
        assertEquals("report-provider-metrics", get(ReportProviderMetricsAgentWorkerCommand.class));
        assertEquals(
                "report-provider-transfer-metrics",
                get(ReportProviderTransferMetricsAgentWorkerCommand.class));
        assertEquals(
                "request-user-opt-in-accounts",
                get(RequestUserOptInAccountsAgentWorkerCommand.class));
        assertEquals(
                "send-accounts-holders-to-update-service",
                get(SendAccountsHoldersToUpdateServiceAgentWorkerCommand.class));
        assertEquals(
                "send-fetched-data-to-data-availability-tracker",
                get(SendFetchedDataToDataAvailabilityTrackerAgentWorkerCommand.class));
        assertEquals(
                "send-accounts-to-update-service",
                get(SendAccountsToUpdateServiceAgentWorkerCommand.class));
        assertEquals(
                "send-data-for-processing", get(SendDataForProcessingAgentWorkerCommand.class));
        assertEquals(
                "send-psd2payment-classification-to-update-service",
                get(SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand.class));
        assertEquals("set-credentials-status", get(SetCredentialsStatusAgentWorkerCommand.class));
        assertEquals("transfer", get(TransferAgentWorkerCommand.class));
        assertEquals(
                "update-credentials-status", get(UpdateCredentialsStatusAgentWorkerCommand.class));
        assertEquals(
                "validate-provider-agent-worker-status",
                get(ValidateProviderAgentWorkerStatus.class));
    }

    private String get(Class<? extends AgentWorkerCommand> klass) {
        return WorkerCommandNameFormatter.getCommandName(klass);
    }
}
