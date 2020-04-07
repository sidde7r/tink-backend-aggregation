package se.tink.backend.aggregation.workers.commands;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.uuid.UUIDUtils;

@RunWith(MockitoJUnitRunner.class)
public class RefreshCommandChainEventTriggerCommandTest {

    private Credentials validCredentials;
    private String appId;
    private String correlationId;
    private CredentialsEventProducer credentialsEventProducer;
    private String clusterId;
    private boolean manual;
    private Set<RefreshableItem> refreshableItems;

    @Before
    public void setup() {
        this.credentialsEventProducer = mock(CredentialsEventProducer.class);
        this.validCredentials = buildValidCredentials();
        this.appId = UUIDUtils.generateUUID();
        this.correlationId = UUIDUtils.generateUUID();
        this.clusterId = "clusterId";
        this.manual = true;
        this.refreshableItems = Collections.emptySet();
    }

    @Test
    public void testCredentialsRefreshStartEventCommand() throws Exception {
        RefreshCommandChainEventTriggerCommand refreshCommandChainEventTriggerCommand =
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        correlationId,
                        validCredentials,
                        appId,
                        refreshableItems,
                        manual,
                        clusterId);
        Assertions.assertThat(refreshCommandChainEventTriggerCommand.execute())
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
