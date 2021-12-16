package src.agent_sdk.sdk.test.utils.serialization.local_date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDMYFormatDeserializer;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateOffsetDeserializer;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateSerializer;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeDeserializer;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeSerializer;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LocalDateSerializationTest {

    @Test
    public void testDeserialize() {
        LocalDate expectedDate = LocalDate.of(2017, 12, 24);
        LocalDateTime expectedDateTime = LocalDateTime.of(2017, 12, 24, 12, 23, 34);
        String serializedData =
                "{\"test1\":\"2017-12-24\",\"test2\":\"24/12/2017\",\"test3\":\"2017-12-24T00:00:00Z\",\"test4\":\"2017-12-24T12:23:34Z\"}";
        DeserializationModel model =
                SerializationUtils.deserializeFromString(
                        serializedData, DeserializationModel.class);

        Assert.assertEquals(expectedDate, model.getTest1());
        Assert.assertEquals(expectedDate, model.getTest2());
        Assert.assertEquals(expectedDate, model.getTest3());
        Assert.assertEquals(expectedDateTime, model.getTest4());
    }

    @Test
    public void testSerialize() {
        LocalDate test1 = LocalDate.of(2017, 12, 24);
        LocalDateTime test2 = LocalDateTime.of(2017, 12, 24, 12, 23, 34);
        SerializationModel model = new SerializationModel(test1, test2);

        String serializedModel = SerializationUtils.serializeToString(model);

        String expectedString = "{\"test1\":\"2017-12-24\",\"test2\":\"2017-12-24T12:23:34\"}";
        Assert.assertEquals(expectedString, serializedModel);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class SerializationModel {
        @JsonSerialize(using = LocalDateSerializer.class)
        private final LocalDate test1;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private final LocalDateTime test2;

        public SerializationModel(LocalDate test1, LocalDateTime test2) {
            this.test1 = test1;
            this.test2 = test2;
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class DeserializationModel {

        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate test1;

        @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
        private LocalDate test2;

        @JsonDeserialize(using = LocalDateOffsetDeserializer.class)
        private LocalDate test3;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime test4;

        public LocalDate getTest1() {
            return test1;
        }

        public LocalDate getTest2() {
            return test2;
        }

        public LocalDate getTest3() {
            return test3;
        }

        public LocalDateTime getTest4() {
            return test4;
        }
    }
}
