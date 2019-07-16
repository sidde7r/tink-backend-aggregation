package se.tink.backend.aggregation.workers.commands;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.uuid.UUIDUtils;

@RunWith(MockitoJUnitRunner.class)
public class CredentialsRefreshStartEventCommandTest {

    private Credentials validCredentials;
    private String appId;
    private CredentialsEventProducer credentialsEventProducer;

    @Before
    public void setup() {
        this.credentialsEventProducer = Mockito.mock(CredentialsEventProducer.class);
        this.validCredentials = buildValidCredentials();
        this.appId = UUIDUtils.generateUUID();
    }

    @Test
    public void testCredentialsRefreshStartEventCommand() throws Exception {
        se.tink.backend.aggregation.workers.commands.CredentialsRefreshStartEventCommand
                credentialsRefreshStartEventCommand =
                        new se.tink.backend.aggregation.workers.commands
                                .CredentialsRefreshStartEventCommand(
                                credentialsEventProducer, validCredentials, appId);
        Assertions.assertThat(credentialsRefreshStartEventCommand.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    private Credentials buildValidCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(UUIDUtils.generateUUID());
        credentials.setUserId(UUIDUtils.generateUUID());
        credentials.setProviderName(UUIDUtils.generateUUID());
        return credentials;
    }
}
