package se.tink.agent.sdk.test.state;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.storage.SerializableStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SerializableStorageTest {

    @Test
    public void testSerialization() {
        // This is exactly what PersistentStorage is serialized into.
        String expected = "{\"foo\":\"bar\",\"hello\":\"world\"}";

        SerializableStorage storage = new SerializableStorage();
        storage.put("hello", "world");
        storage.put("foo", "bar");

        String serialized = SerializationUtils.serializeToString(storage);
        Assert.assertEquals(expected, serialized);
    }

    @Test
    public void testDeserialization() {
        String serialized = "{\"foo\":\"bar\",\"hello\":\"world\"}";
        SerializableStorage storage =
                SerializationUtils.deserializeFromString(serialized, SerializableStorage.class);

        Assert.assertEquals(Optional.of("bar"), storage.tryGet("foo"));
        Assert.assertEquals(Optional.of("world"), storage.tryGet("hello"));
        Assert.assertEquals(Optional.empty(), storage.tryGet("something else"));
    }
}
