package se.tink.backend.aggregation.utils.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return LocalDateTime.parse(
                jsonParser.readValueAs(String.class), DateTimeFormatter.ISO_DATE_TIME);
    }
}
