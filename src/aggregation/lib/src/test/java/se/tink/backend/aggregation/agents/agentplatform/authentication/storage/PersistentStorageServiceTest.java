package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PersistentStorageServiceTest {

    @Test
    public void shouldReadCredentialsFromPersistentStorage() {
        // given
        String expectedValue = "value1";
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("key1", expectedValue);
        PersistentStorageService objectUnderTest = new PersistentStorageService(persistentStorage);
        // when
        AgentAuthenticationPersistedData result = objectUnderTest.readFromAgentPersistentStorage();
        // then
        Assert.assertEquals(expectedValue, result.valuesCopy().get("key1"));
    }

    @Test
    public void shouldRewriteToPersistentStorage() {
        // given
        Map<String, String> data = new HashMap<>();
        data.put("newKey", "value");
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(data);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("oldKey", "oldValue");
        PersistentStorageService objectUnderTet = new PersistentStorageService(persistentStorage);
        // when
        objectUnderTet.writeToAgentPersistentStorage(agentAuthenticationPersistedData);
        // then
        Assert.assertFalse(persistentStorage.containsKey("oldKey"));
        Assert.assertTrue(persistentStorage.containsKey("newKey"));
    }
}
