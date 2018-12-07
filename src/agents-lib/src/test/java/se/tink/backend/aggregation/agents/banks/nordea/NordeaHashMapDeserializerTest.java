package se.tink.backend.aggregation.agents.banks.nordea;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.social.security.time.SwedishTimeRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class NordeaHashMapDeserializerTest {

    public static class DateDeserialize {

        @Rule
        public SwedishTimeRule timeRule = new SwedishTimeRule();

        @Test
        public void deserializesDateMapWithoutMS() throws IOException {
            NordeaHashMapDeserializer.Date deserializer = new NordeaHashMapDeserializer.Date();

            Date dateMSMissing = deserializer
                    .deserialize(mockJsonParserReturning("2016-05-30T12:00:00+02:00"), null);

            Date expected = new DateTime()
                    .withYear(2016)
                    .withMonthOfYear(5)
                    .withDayOfMonth(30)
                    .withHourOfDay(12)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)
                    .withZone(DateTimeZone.forOffsetHours(2))
                    .toDate();

            assertThat(dateMSMissing).isEqualTo(expected);
        }

        @Test
        public void deserializesDateMapWithMS() throws IOException {
            NordeaHashMapDeserializer.Date deserializer = new NordeaHashMapDeserializer.Date();

            Date dateMSSpecified = deserializer
                    .deserialize(mockJsonParserReturning("2016-05-30T12:00:00.123+02:00"), null);

            Date expected = new DateTime()
                    .withYear(2016)
                    .withMonthOfYear(5)
                    .withDayOfMonth(30)
                    .withHourOfDay(12)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(123)
                    .withZone(DateTimeZone.forOffsetHours(2))
                    .toDate();

            assertThat(dateMSSpecified).isEqualTo(expected);
        }

        private static JsonParser mockJsonParserReturning(String value) throws IOException {
            JsonNode valueNode = mock(JsonNode.class);
            when(valueNode.asText()).thenReturn(value);

            JsonNode mapNode = mock(JsonNode.class);
            when(mapNode.get("$")).thenReturn(valueNode);

            ObjectCodec objectCodec = mock(ObjectCodec.class);
            when(objectCodec.readTree(any(JsonParser.class))).thenReturn(mapNode);

            JsonParser jsonParser = mock(JsonParser.class);
            when(jsonParser.getCodec()).thenReturn(objectCodec);

            return jsonParser;
        }
    }

}
