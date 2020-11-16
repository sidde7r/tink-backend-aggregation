package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.junit.Test;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UpgradingPersistentStorageServiceTest {

    @Test
    public void shouldUpgradeWhenReadingFirstTime() {
        // given
        PersistentStorage ps = new PersistentStorage();
        AgentPlatformStorageMigrator su = mock(AgentPlatformStorageMigrator.class);
        UpgradingPersistentStorageService out = new UpgradingPersistentStorageService(ps, su);
        AgentAuthenticationPersistedData expectedData =
                new AgentAuthenticationPersistedData(new HashMap<>());

        when(su.migrate(ps)).thenReturn(expectedData);

        // when
        AgentAuthenticationPersistedData actualData = out.readFromAgentPersistentStorage();

        // then
        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    public void shouldMarkAsUpgradedAfterWrite() {
        // given
        PersistentStorage ps = new PersistentStorage();
        UpgradingPersistentStorageService out =
                new UpgradingPersistentStorageService(ps, mock(AgentPlatformStorageMigrator.class));
        AgentAuthenticationPersistedData data =
                new AgentAuthenticationPersistedData(new HashMap<>());

        // then
        assertThat(ps).doesNotContainKey(UpgradingPersistentStorageService.MARKER);

        // when
        out.writeToAgentPersistentStorage(data);

        // then
        assertThat(ps).containsKey(UpgradingPersistentStorageService.MARKER);
    }

    @Test
    public void shouldNotUpgradeWhenAlreadyUpgraded() {
        // given
        PersistentStorage ps = new PersistentStorage();
        AgentPlatformStorageMigrator su = mock(AgentPlatformStorageMigrator.class);
        UpgradingPersistentStorageService out = new UpgradingPersistentStorageService(ps, su);

        // when
        markAsUpgraded(ps);
        out.readFromAgentPersistentStorage();

        // then
        verify(su, never()).migrate(ps);
    }

    private void markAsUpgraded(PersistentStorage ps) {
        ps.put(UpgradingPersistentStorageService.MARKER, true);
    }
}
