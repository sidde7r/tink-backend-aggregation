package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors;

@RunWith(MockitoJUnitRunner.class)
public class ExpireSessionAgentWorkerCommandTest {
    private StatusUpdater statusUpdater;
    private ArgumentCaptor<CredentialsStatus> statusArgumentCaptor;
    private ArgumentCaptor<ConnectivityError> errorArgumentCaptor;

    @Before
    public void setup() {
        statusUpdater = Mockito.mock(StatusUpdater.class);
        statusArgumentCaptor = ArgumentCaptor.forClass(CredentialsStatus.class);
        errorArgumentCaptor = ArgumentCaptor.forClass(ConnectivityError.class);
    }

    @Test
    public void ensureManualRefreshesReturnCommandResultContinueAndStatusUpdated()
            throws Exception {
        Credentials credentials = createCredentials(new Date());
        Provider provider = createProvider(Provider.AccessType.OPEN_BANKING);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(true, statusUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verifyZeroInteractions(statusUpdater);
    }

    @Test
    public void ensureAccessTypeNullReturnCommandResultContinueAndStatusUpdated() throws Exception {
        Credentials credentials = createCredentials(new Date());
        Provider provider = createProvider(null);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, statusUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verifyZeroInteractions(statusUpdater);
    }

    @Test
    public void ensureAccessTypeOtherReturnCommandResultContinueAndStatusUpdated()
            throws Exception {
        Credentials credentials = createCredentials(new Date());
        Provider provider = createProvider(Provider.AccessType.OTHER);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, statusUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verifyZeroInteractions(statusUpdater);
    }

    @Test
    public void ensureSessionExpiryDateNullReturnCommandResultContinueAndStatusUpdated()
            throws Exception {
        Credentials credentials = createCredentials(null);
        Provider provider = createProvider(Provider.AccessType.OPEN_BANKING);

        ExpireSessionAgentWorkerCommand expireSessionAgentWorkerCommand =
                new ExpireSessionAgentWorkerCommand(false, statusUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verifyZeroInteractions(statusUpdater);
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
                new ExpireSessionAgentWorkerCommand(false, statusUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verifyZeroInteractions(statusUpdater);
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
                new ExpireSessionAgentWorkerCommand(false, statusUpdater, credentials, provider);

        Assertions.assertThat(expireSessionAgentWorkerCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        statusArgumentCaptor.capture(), any(), errorArgumentCaptor.capture());
        Assert.assertEquals(CredentialsStatus.SESSION_EXPIRED, statusArgumentCaptor.getValue());
        Assert.assertEquals(
                AuthorizationErrors.SESSION_EXPIRED.toString(),
                errorArgumentCaptor.getValue().getDetails().getReason());
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
