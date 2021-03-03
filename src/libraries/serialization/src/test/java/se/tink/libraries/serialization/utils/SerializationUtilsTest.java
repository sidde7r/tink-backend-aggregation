package se.tink.libraries.serialization.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Test;

public class SerializationUtilsTest {

    @Test
    public void shouldThrowExceptionWhenBinaryToDeserializeIsNull() {
        // given
        byte[] binary = null;

        // when
        Throwable t =
                catchThrowable(
                        () -> SerializationUtils.deserializeFromBinary(binary, TargetClass.class));

        // then
        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldRethrowExceptionFromObjectMapper() {
        // given
        byte[] binary = {0, 1, 2, 3, 4};

        // when
        Throwable t =
                catchThrowable(
                        () -> SerializationUtils.deserializeFromBinary(binary, TargetClass.class));

        // then
        assertThat(t).isInstanceOf(IOException.class);
    }

    @Test
    public void shouldSerializeToBinary() throws IOException {
        // given
        byte[] expectedResult =
                new byte[] {58, 41, 10, 1, 73, 116, 101, 115, 116, 83, 116, 114, 105, 110, 103};

        // when
        byte[] result = SerializationUtils.serializeToBinary("testString");

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void shouldThrowExceptionWhenSerializationIsFailed() {
        // given

        // when
        Throwable t = catchThrowable(() -> SerializationUtils.serializeToBinary(new TargetClass()));

        // then
        assertThat(t).isInstanceOf(IOException.class);
    }

    static class TargetClass {
        TargetClass selfReference;

        public TargetClass() {
            this.selfReference = this;
        }
    }

    @Test
    public void shouldSerializeAndDeserializeJavaTimeCorrectly() {
        // given
        LocalDate localDate = LocalDate.of(1999, 10, 20);
        LocalTime localTime = LocalTime.of(10, 50, 45, 123456);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        // when
        String serializedLocalDate = SerializationUtils.serializeToString(localDate);
        String serializedLocalTime = SerializationUtils.serializeToString(localTime);
        String serializedLocalDateTime = SerializationUtils.serializeToString(localDateTime);

        LocalDate deserializedLocalDate =
                SerializationUtils.deserializeFromString(serializedLocalDate, LocalDate.class);
        LocalTime deserializedLocalTime =
                SerializationUtils.deserializeFromString(serializedLocalTime, LocalTime.class);
        LocalDateTime deserializedLocalDateTime =
                SerializationUtils.deserializeFromString(
                        serializedLocalDateTime, LocalDateTime.class);

        // then
        assertThat(deserializedLocalDate).isEqualTo(localDate);
        assertThat(deserializedLocalTime).isEqualTo(localTime);
        assertThat(deserializedLocalDateTime).isEqualTo(localDateTime);
    }
}
