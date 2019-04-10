package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62DateDeserializer;

public class AmericanExpressV62DateDeserializerTest {

    public static String CHARGE_DATE =
            "{" + "\"formattedDate\": \"12 maj 2018\",\n" + "\"rawValue\": 20180512\n" + "}";
    public static String WRONG_CHARGE_DATE =
            "{" + "\"formattedDate\": \"12 maj 2018\",\n" + "\"rawValue\": 99999999\n" + "}";

    private ObjectMapper mapper;
    private AmericanExpressV62DateDeserializer deserializer;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
        deserializer = new AmericanExpressV62DateDeserializer();
    }

    @Test
    public void deserializeDate_properInput() throws IOException {
        Date deserializedDate = deserializeDate(CHARGE_DATE);
        Date actualDate =
                new DateTime().withYear(2018).withMonthOfYear(5).withDayOfMonth(12).toDate();
        assertThat(deserializedDate, instanceOf(Date.class));
        assertTrue(DateUtils.isSameDay(actualDate, deserializedDate));
    }

    private Date deserializeDate(String json) throws IOException {
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        return deserializer.deserialize(parser, ctxt);
    }

    @Test(expected = IllegalStateException.class)
    public void deserializeDate_wrongInput() throws IOException {
        deserializeDate(WRONG_CHARGE_DATE);
    }
}
