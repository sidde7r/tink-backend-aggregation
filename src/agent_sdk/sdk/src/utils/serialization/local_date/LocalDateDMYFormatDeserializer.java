package se.tink.agent.sdk.utils.serialization.local_date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateDMYFormatDeserializer extends StdDeserializer<LocalDate> {

    private LocalDateDMYFormatDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return LocalDate.parse(
                jsonParser.readValueAs(String.class), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
