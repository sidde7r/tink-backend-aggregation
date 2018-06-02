package se.tink.backend.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KVPairTest {

    ObjectMapper objectMapper = new ObjectMapper();
    String serializedKVPair = "{\"key\":\"key\",\"value\":\"value\"}";

    @Test
    public void testKVPairSerialization() throws IOException {
        assertEquals(
                serializedKVPair,
                objectMapper.writeValueAsString(new KVPair<>("key", "value")));
    }

    @Test
    public void testKVPairDeserialization() throws IOException {
        assertEquals(
                new KVPair<>("key", "value"),
                objectMapper.readValue(serializedKVPair, KVPair.class));
    }

}
