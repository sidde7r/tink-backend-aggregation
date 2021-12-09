package se.tink.agent.sdk.user_interaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Objects;

public class SwedishMobileBankIdInfoSerializer extends StdSerializer<SwedishMobileBankIdInfo> {
    public SwedishMobileBankIdInfoSerializer() {
        this(null);
    }

    public SwedishMobileBankIdInfoSerializer(Class<SwedishMobileBankIdInfo> t) {
        super(t);
    }

    @Override
    public void serialize(
            SwedishMobileBankIdInfo swedishMobileBankIdInfo,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider)
            throws IOException {
        if (Objects.nonNull(swedishMobileBankIdInfo.autostartToken)) {
            jsonGenerator.writeRawValue(swedishMobileBankIdInfo.autostartToken);
        }
    }
}
