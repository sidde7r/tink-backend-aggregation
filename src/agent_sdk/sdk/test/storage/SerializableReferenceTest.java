package se.tink.agent.sdk.test.state;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SerializableReferenceTest {
    @Test
    public void testSerde() {
        // TODO: add complex types as test cases.
        List<SerializationTestCase<?>> testCases = new ArrayList<>();
        testCases.add(new SerializationTestCase<>("foobar", String.class, "foobar"));
        testCases.add(new SerializationTestCase<>(1234, Integer.class, "1234"));
        testCases.add(new SerializationTestCase<>(12.34, Double.class, "12.34"));
        testCases.add(new SerializationTestCase<>(true, Boolean.class, "true"));

        for (SerializationTestCase<?> testCase : testCases) {
            SerializableReference reference = SerializableReference.from(testCase.getInput());
            String serializedReference = SerializationUtils.serializeToString(reference);

            String expectedSerialization =
                    String.format("{\"reference\":\"%s\"}", testCase.getSerializedValue());
            Assert.assertEquals(
                    "A reference value failed to serialize.",
                    expectedSerialization,
                    serializedReference);

            SerializableReference deserializedReference =
                    SerializationUtils.deserializeFromString(
                            serializedReference, SerializableReference.class);

            Object referenceValue = deserializedReference.get(testCase.getDeserializationType());
            Assert.assertEquals(
                    "The reference value has changed between serialization and deserialization.",
                    testCase.getInput(),
                    referenceValue);
        }
    }

    private static class SerializationTestCase<T> {
        private final T input;
        private final Class<T> deserializationType;
        private final String serializedValue;

        public SerializationTestCase(
                T input, Class<T> deserializationType, String serializedValue) {
            this.input = input;
            this.deserializationType = deserializationType;
            this.serializedValue = serializedValue;
        }

        public T getInput() {
            return input;
        }

        public Class<T> getDeserializationType() {
            return deserializationType;
        }

        public String getSerializedValue() {
            return serializedValue;
        }
    }
}
