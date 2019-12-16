package se.tink.backend.aggregation.agents.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.Href;

public class HrefTest {

    private static final String HREF_JSON = "{\"href\":null}";
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getHref() throws IOException {
        Href href = mapper.readValue(HREF_JSON, Href.class);
        assertNull(href.getHref());
    }

    @Test(expected = NullPointerException.class)
    public void getHrefCheckNotNull() throws IOException {
        Href href = mapper.readValue(HREF_JSON, Href.class);
        href.getHrefCheckNotNull();
    }

    @Test
    public void getNullableHref() throws JsonProcessingException {
        Href href = new Href();
        assertEquals(mapper.writeValueAsString(href.getNullableHref()), "\"\"");
    }

    @Test
    public void testNullHref() throws JsonProcessingException {
        Href href = new Href();
        assertEquals(mapper.writeValueAsString(href), HREF_JSON);
    }
}
