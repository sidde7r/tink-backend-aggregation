package se.tink.libraries.serialization.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;

public class JsonFlattenerTest {

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
        Map<String, String> map = JsonFlattener.flattenJsonToMap(JSON);
        for (Entry<String, String> entry : map.entrySet()) {
            Assert.assertTrue(map.containsKey(entry.getKey()));
            Assert.assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testCreatingKeyValuesMaximumRecursionLevelReached() throws IOException {
        String veryLongJson =
                "{\n"
                        + "   \"Port\":\n"
                        + "   {\n"
                        + "       \"@alias\": \"defaultHttp\",\n"
                        + "       \"Enabled\": \"true\",\n"
                        + "       \"Number\": \"10092\",\n"
                        + "       \"Protocol\": \"http\",\n"
                        + "       \"KeepAliveTimeout\": \"20000\",\n";
        for (int i = 0; i < JsonFlattener.MAX_RECURSION_DEPTH_EXTRACT_SENSITIVE_VALUES; ++i) {
            veryLongJson +=
                    "       \"ThreadPool"
                            + i
                            + "\":\n"
                            + "       {\n"
                            + "           \"@enabled\": \"false\",\n"
                            + "           \"Max\": \"150\",\n"
                            + "           \"ThreadPriority\": \"5\"\n"
                            + "       },\n";
        }

        veryLongJson +=
                "       \"ExtendedProperties\":\n"
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

        JsonFlattener.flattenJsonToMap(veryLongJson);
    }
}
