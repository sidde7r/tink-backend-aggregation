package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.JsonFlattener;

public class JsonFlattenerTest {

    public static final ImmutableMap<String, String> EXPECTED_OUTPUT =
            ImmutableMap.<String, String>builder()
                    .put("Port.ThreadPool.Max", "150")
                    .put("Port.ThreadPool.@enabled", "false")
                    .put("Port.Number", "10092")
                    .put("Port.ExtendedProperties.Property[0].@name", "connectionTimeout")
                    .put("Port.ThreadPool.ThreadPriority", "5")
                    .put("Port.Protocol", "http")
                    .put("Port.KeepAliveTimeout", "20000")
                    .put("Port.ExtendedProperties.Property[0].$", "20000")
                    .put("Port.@alias", "defaultHttp")
                    .put("Port.Enabled", "true")
                    .build();
    private static final String JSON =
            "{\n"
                    + "   \"Port\":\n"
                    + "   {\n"
                    + "       \"@alias\": \"defaultHttp\",\n"
                    + "       \"Enabled\": \"true\",\n"
                    + "       \"Number\": \"10092\",\n"
                    + "       \"Protocol\": \"http\",\n"
                    + "       \"KeepAliveTimeout\": \"20000\",\n"
                    + "       \"ThreadPool\":\n"
                    + "       {\n"
                    + "           \"@enabled\": \"false\",\n"
                    + "           \"Max\": \"150\",\n"
                    + "           \"ThreadPriority\": \"5\"\n"
                    + "       },\n"
                    + "       \"ExtendedProperties\":\n"
                    + "       {\n"
                    + "           \"Property\":\n"
                    + "           [                         \n"
                    + "               {\n"
                    + "                   \"@name\": \"connectionTimeout\",\n"
                    + "                   \"UrL\": \"https://www.swedbank.se\",\n"
                    + "                   \"StringWithQuotes\": \"ab\\\"c\\\"d\",\n"
                    + "                   \"$\": \"20000\"\n"
                    + "               }\n"
                    + "           ]\n"
                    + "       }\n"
                    + "   }\n"
                    + "}";

    @Test
    public void testCreatingKeyValues() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map = JsonFlattener.flattenJsonToMap(JSON);
        for (Entry<String, String> entry : map.entrySet()) {
            Assert.assertTrue(map.containsKey(entry.getKey()));
            Assert.assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }
}
