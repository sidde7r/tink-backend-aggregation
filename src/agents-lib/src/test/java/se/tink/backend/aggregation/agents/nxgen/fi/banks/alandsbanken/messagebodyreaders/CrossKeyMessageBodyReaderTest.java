package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.messagebodyreaders;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.messagebodyreaders.CrossKeyMessageBodyReader;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class CrossKeyMessageBodyReaderTest {


    private CrossKeyMessageBodyReader crossKeyMessageBodyReader;

    @Before
    public void setUp() throws Exception {
        crossKeyMessageBodyReader = new CrossKeyMessageBodyReader(getClass().getPackage());
    }

    @Test
    public void canConvertNull() throws Exception {
        InputStream input = null;

        InputStream output = testRemoveUnwantedCharacters(input);

        assertNull(output);
    }

    @Test
    public void canConvertEmptyString() throws Exception {
        String input = "";

        String output = testRemoveUnwantedCharacters(input);

        assertEquals("", output);
    }

    @Test
    public void canConvertArbitraryString() throws Exception {
        String input = "sdfds";

        String output = testRemoveUnwantedCharacters(input);

        assertEquals("sdfds", output);
    }

    @Test
    public void convertsWithLineEnding() throws Exception {
        String input = ")]}',\n9348u3\nr0djfewfj";

        String output = testRemoveUnwantedCharacters(input);

        assertEquals("9348u3\nr0djfewfj", output);
    }

    @Test
    public void convertsWithoutComma() throws Exception {
        String input = ")]}'\n9348u3\nr0djfewfj";

        String output = testRemoveUnwantedCharacters(input);

        assertEquals("9348u3\nr0djfewfj", output);
    }

    @Test
    public void objectsFromSpecificPackageAreReadable() throws Exception {
        assertFalse(crossKeyMessageBodyReader.isReadable(Object.class, null, null, null));
        assertTrue(crossKeyMessageBodyReader.isReadable(TestObject.class, null, null, null));
    }

    private String testRemoveUnwantedCharacters(String input) throws IOException {
        return IOUtils.toString(testRemoveUnwantedCharacters(toInputStream(input, Charsets.UTF_8)), Charsets.UTF_8);
    }

    private InputStream testRemoveUnwantedCharacters(InputStream entityStream) throws IOException {
        return crossKeyMessageBodyReader.removeUnwantedCharacters(entityStream);
    }

    private class TestObject{}
}
