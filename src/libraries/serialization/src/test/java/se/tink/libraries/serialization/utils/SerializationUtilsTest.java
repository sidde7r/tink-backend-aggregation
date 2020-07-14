package se.tink.libraries.serialization.utils;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilsTest {

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenBinaryToDeserializeIsNull() throws IOException {
        // Arrange
        byte[] binary = null;

        // Act & Assert
        SerializationUtils.deserializeFromBinary(binary, TargetClass.class);
    }

    @Test(expected = IOException.class)
    public void shouldRethrowExceptionFromObjectMapper() throws IOException {
        // Arrange
        byte[] binary = {0, 1, 2, 3, 4};

        // Act & Assert
        SerializationUtils.deserializeFromBinary(binary, TargetClass.class);
    }

    @Test
    public void shouldSerializeToBinary() throws IOException {
        // Act
        byte[] testBinary = SerializationUtils.serializeToBinary("testString");

        // Assert
        Assert.assertNotNull(testBinary);
        Assert.assertNotEquals(0, testBinary.length);
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenSerializationIsFailed() throws IOException {
        // Act & Assert
        SerializationUtils.serializeToBinary(new TargetClass());
    }

    static class TargetClass {
        TargetClass selfReference;

        public TargetClass() {
            this.selfReference = this;
        }
    }
}
