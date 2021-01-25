package se.tink.backend.aggregation.workers.commands;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

@RunWith(MockitoJUnitRunner.class)
public class ExpireSessionAgentWorkerCommandTest {
    private SystemUpdater systemUpdater;

    @Before
    public void setup() {
        systemUpdater = Mockito.mock(SystemUpdater.class);
        Mockito.doNothing()
                .when(systemUpdater)
                .updateCredentialsExcludingSensitiveInformation(
                        Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void ensureManualRefreshesReturnCommandResultContinueAndStatusUpdated()
            throws Exception {
        Credentials credentials = createCredentials(new Date());
        Provider provider = createProvider(Provider.AccessType.OPEN_BANKING);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(true, systemUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Assertions.assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.UPDATED);
    }

    @Test
    public void ensureAccessTypeNullReturnCommandResultContinueAndStatusUpdated() throws Exception {
        Credentials credentials = createCredentials(new Date());
        Provider provider = createProvider(null);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, systemUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Assertions.assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.UPDATED);
    }

    @Test
    public void ensureAccessTypeOtherReturnCommandResultContinueAndStatusUpdated()
            throws Exception {
        Credentials credentials = createCredentials(new Date());
        Provider provider = createProvider(Provider.AccessType.OTHER);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, systemUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Assertions.assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.UPDATED);
    }

    @Test
    public void ensureSessionExpiryDateNullReturnCommandResultContinueAndStatusUpdated()
            throws Exception {
        Credentials credentials = createCredentials(null);
        Provider provider = createProvider(Provider.AccessType.OPEN_BANKING);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, systemUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Assertions.assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.UPDATED);
    }

    @Test
    public void ensureSessionExpiredTomorrowReturnCommandResultAbortAndStatusUpdated()
            throws Exception {
        Date tomorrow =
                Date.from(
                        LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());

        Credentials credentials = createCredentials(tomorrow);
        Provider provider = createProvider(Provider.AccessType.OPEN_BANKING);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, systemUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Assertions.assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.UPDATED);
    }

    @Test
    public void ensureSessionExpiredYesterdayReturnCommandResultAbortAndStatusSessionExpired()
            throws Exception {
        Date yesterday =
                Date.from(
                        LocalDateTime.now()
                                .minusDays(1)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        Credentials credentials = createCredentials(yesterday);
        Provider provider = createProvider(Provider.AccessType.OPEN_BANKING);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, systemUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.ABORT);
        Assertions.assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.SESSION_EXPIRED);
    }

    private static Credentials createCredentials(Date sessionExpireDate) {
        Credentials credentials = new Credentials();

        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setSessionExpiryDate(sessionExpireDate);

        return credentials;
    }

    private static Provider createProvider(Provider.AccessType accessType) {
        Provider provider = new Provider();

        provider.setAccessType(accessType);

        return provider;
    }
}
