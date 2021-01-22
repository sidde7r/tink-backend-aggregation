package se.tink.libraries.amount;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.chrono.AvailableDateInformation;

public class AvailableDateInformationSerializationTest {

    private static final String EXAMPLE_JSON =
            "{\"date\":\"2002-04-11\",\"instant\":\"2002-04-11T12:15:30.345Z\"}";

    @Test
    public void shouldSerializeDateObjectToISOString() throws Exception {
        // given
        Instant givenInstant = Instant.parse("2002-04-11T12:15:30.345Z");
        LocalDate givenLocalDate = LocalDate.parse("2002-04-11");

        AvailableDateInformation givenAvailableDateInformation = new AvailableDateInformation();
        givenAvailableDateInformation.setDate(givenLocalDate);
        givenAvailableDateInformation.setInstant(givenInstant);

        ObjectMapper om = new ObjectMapper();

        // when
        String result = om.writeValueAsString(givenAvailableDateInformation);

        // then
        Assert.assertEquals(EXAMPLE_JSON, result);
    }

    @Test
    public void shouldDeserializeDateObjectToISOString() throws Exception {

        // given
        ObjectMapper om = new ObjectMapper();

        // when
        AvailableDateInformation result =
                om.readValue(EXAMPLE_JSON, AvailableDateInformation.class);

        // then
        Instant expectedInstant = Instant.parse("2002-04-11T12:15:30.345Z");
        LocalDate expectedLocalDate = LocalDate.parse("2002-04-11");

        Assert.assertEquals(expectedInstant, result.getInstant());
        Assert.assertEquals(expectedLocalDate, result.getDate());
    }
}
