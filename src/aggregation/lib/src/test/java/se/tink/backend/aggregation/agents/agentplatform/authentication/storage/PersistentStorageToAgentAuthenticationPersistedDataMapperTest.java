package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PersistentStorageToAgentAuthenticationPersistedDataMapperTest {

    private PersistentStorageToAgentAuthenticationPersistedDataMapper objectUnderTest;

    @Before
    public void init() {
        objectUnderTest = new PersistentStorageToAgentAuthenticationPersistedDataMapper();
    }

    @Test
    public void shouldMapPersistedDataToAgentAuthenticationPersistedData() {
        // given
        PersistentStorage source = new PersistentStorage();
        source.put("key1", "value1");
        source.put("key2", "value2");
        // when
        AgentAuthenticationPersistedData result = objectUnderTest.mapTo(source);
        // then
        Assert.assertEquals("value1", result.valuesCopy().get("key1"));
        Assert.assertEquals("value2", result.valuesCopy().get("key2"));
    }

    @Test
    public void shouldMapAgentAuthenticationPersistedDataToPersistedData() {
        // given
        HashMap<String, String> data = new HashMap<>();
        data.put("stringKey", "stringValue");
        AgentAuthenticationPersistedData source = new AgentAuthenticationPersistedData(data);
        // when
        PersistentStorage result = objectUnderTest.mapFrom(source);
        // then
        Assert.assertTrue(result.containsKey("stringKey"));
        Assert.assertEquals("stringValue", result.get("stringKey"));
    }
}
