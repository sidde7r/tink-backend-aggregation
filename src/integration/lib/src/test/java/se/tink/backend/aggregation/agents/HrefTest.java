package se.tink.backend.aggregation.agents;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;

public class HrefTest {

    private String hrefJson = "{\"href\":null}";
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getHref() throws IOException {
        Href href = mapper.readValue(hrefJson, Href.class);
        assertEquals(href.getHref(), "");
    }

    @Test(expected = NullPointerException.class)
    public void getHrefCheckNotNull() throws IOException {
        Href href = mapper.readValue(hrefJson, Href.class);
        href.getHrefCheckNotNull();
    }
}
