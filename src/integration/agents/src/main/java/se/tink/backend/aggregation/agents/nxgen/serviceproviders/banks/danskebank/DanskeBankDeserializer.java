package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankDeserializer {
    // This is a hack to handle that the response Content-Type is application/octet-stream
    public static <T> T convertStringToObject(String responseString, Class<T> clazz) {
        T deserialized = SerializationUtils.deserializeFromString(responseString, clazz);
        if (deserialized == null) {
            throw new IllegalStateException(
                    String.format("Could not deserialize String [%s] to Class [%s]",
                            responseString, clazz.getName()));
        }
        return deserialized;
    }
}
