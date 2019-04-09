package se.tink.libraries.dropwizard;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDDeserializer;
import se.tink.libraries.uuid.UUIDSerializer;

public class ObjectMapperFactory {

    public static ObjectMapper createForApiUse() {
        ObjectMapper mapper = new ObjectMapper();

        configureForApiUse(mapper);

        return mapper;
    }

    public static void configureForApiUse(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("Special Serialization and Deserialization");
        module.addSerializer(UUID.class, new UUIDSerializer());
        module.addDeserializer(UUID.class, new UUIDDeserializer());

        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
