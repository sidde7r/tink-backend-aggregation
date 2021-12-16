package src.agent_sdk.sdk.test.utils.serialization.body;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import org.junit.Before;
import org.junit.Test;
import se.tink.agent.sdk.utils.serialization.body.NoEscapeOfBackslashMessageBodyWriter;

public class NoEscapeOfBackslashMessageBodyWriterTest {

    private NoEscapeOfBackslashMessageBodyWriter noEscapeBodyWriter;

    @Before
    public void setUp() {
        noEscapeBodyWriter = new NoEscapeOfBackslashMessageBodyWriter(HashMap.class);
    }

    @Test
    public void nothingToSuppressEscaping() throws Exception {
        Map<String, String> input = new HashMap<>();
        input.put("propertyname", "property value");

        String output = testWriteObjectAsString(input, noEscapeBodyWriter);

        assertFalse(output.contains("\\"));
    }

    @Test
    public void suppressEscaping() throws Exception {
        Map<String, String> input = new HashMap<>();
        input.put("propertyname", "property\\/value");

        String output = testWriteObjectAsString(input, noEscapeBodyWriter);

        assertTrue(output.contains("\\"));
    }

    @Test
    public void objectsOfCorrectClassAreWriteable() throws Exception {
        assertTrue(
                noEscapeBodyWriter.isWriteable(
                        HashMap.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void objectsNotOfCorrectClassAreNotWriteable() throws Exception {
        assertFalse(
                noEscapeBodyWriter.isWriteable(
                        TestData.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    private String testWriteObjectAsString(Object o, MessageBodyWriter aBodyWriter)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        aBodyWriter.writeTo(o, o.getClass(), null, new Annotation[0], null, null, baos);
        return baos.toString("UTF-8");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class TestData {
        String name;
        String value;

        public TestData(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
