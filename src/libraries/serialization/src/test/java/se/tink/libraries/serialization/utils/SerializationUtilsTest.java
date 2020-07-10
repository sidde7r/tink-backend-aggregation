package se.tink.libraries.serialization.utils;

import java.io.IOException;
import org.junit.Test;

public class SerializationUtilsTest {

    @Test(expected = NullPointerException.class)
    public void Should_ThrowException_When_BinaryToDeserializeIsNull() throws IOException {
        // Arrange
        byte[] binary = null;

        // Act & Assert
        SerializationUtils.deserializeFromBinary(binary, TargetClass.class);
    }

    @Test(expected = IOException.class)
    public void Should_RethrowException_FromObjectMapper() throws IOException {
        // Arrange
        byte[] binary = {0, 1, 2, 3, 4};

        // Act & Assert
        SerializationUtils.deserializeFromBinary(binary, TargetClass.class);
    }

    static class TargetClass {}
}
