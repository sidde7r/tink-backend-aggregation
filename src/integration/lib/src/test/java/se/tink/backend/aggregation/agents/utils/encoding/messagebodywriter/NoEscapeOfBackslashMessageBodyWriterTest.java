package se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class NoEscapeOfBackslashMessageBodyWriterTest {

    private NoEscapeOfBackslashMessageBodyWriter noEscapeBodyWriter;
    private MessageBodyWriter defaultMessageBodyWriter;

    @Before
    public void setUp() throws Exception {
        noEscapeBodyWriter = new NoEscapeOfBackslashMessageBodyWriter(HashMap.class);

        // a bit strange, but this way we can get the default message body writer for Json
        defaultMessageBodyWriter =
                new TinkHttpClient()
                        .getInternalClient()
                        .getMessageBodyWorkers()
                        .getMessageBodyWriter(
                                HashMap.class, null, null, MediaType.APPLICATION_JSON_TYPE);
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
    public void notApplicableForEscaping() throws Exception {
        TestData input = new TestData("propertyname", "property\\/value");

        String output = testWriteObjectAsString(input, defaultMessageBodyWriter);

        assertTrue(output.contains("\\\\"));
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

    @Test
    public void defaultImplementationAcceptsAll() throws Exception {
        assertTrue(
                defaultMessageBodyWriter.isWriteable(
                        HashMap.class, null, null, MediaType.APPLICATION_JSON_TYPE));
        assertTrue(
                defaultMessageBodyWriter.isWriteable(
                        TestData.class,
                        null,
                        TestData.class.getAnnotations(),
                        MediaType.APPLICATION_JSON_TYPE));
    }

    private String testWriteObjectAsString(Object o, MessageBodyWriter aBodyWriter)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        aBodyWriter.writeTo(o, o.getClass(), null, new Annotation[0], null, null, baos);
        return baos.toString("UTF-8");
    }

    @JsonObject
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
