package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentplatform.authentication.TestPersistedDataObject;

public class StringSerializerTest {

    @Test
    public void shouldSerializeAndDeserializeBackAnObject() {
        // given
        TestPersistedDataObject testPersistedDataObject =
                new TestPersistedDataObject("testValue", 1);
        // when
        TestPersistedDataObject result =
                StringSerializer.deserialize(StringSerializer.serialize(testPersistedDataObject));
        // then
        Assert.assertEquals(testPersistedDataObject, result);
    }

    @Test
    public void shouldSerializeAndDeserializeBackAString() {
        // given
        String value = "value";
        // when
        String result = StringSerializer.deserialize(StringSerializer.serialize(value));
        // then
        Assert.assertEquals(value, result);
    }
}
