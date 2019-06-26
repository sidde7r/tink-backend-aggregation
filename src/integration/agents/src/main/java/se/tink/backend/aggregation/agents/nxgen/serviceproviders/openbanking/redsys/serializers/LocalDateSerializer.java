package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateSerializer extends StdSerializer<LocalDate> {

    public LocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(
            LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString(localDate.format(DateTimeFormatter.ISO_DATE));
    }
}
