package se.tink.agent.sdk.user_interaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import se.tink.backend.agents.rpc.Field;

public class SupplementalInformationSerializer extends StdSerializer<SupplementalInformation> {
    public SupplementalInformationSerializer() {
        this(null);
    }

    public SupplementalInformationSerializer(Class<SupplementalInformation> t) {
        super(t);
    }

    @Override
    public void serialize(
            SupplementalInformation supplementalInformation,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider)
            throws IOException {
        ImmutableList<Field> fields = supplementalInformation.getFields();
        jsonGenerator.writeStartArray(fields.size());
        for (Field field : fields) {
            jsonGenerator.writeObject(field);
        }
        jsonGenerator.writeEndArray();
    }
}
