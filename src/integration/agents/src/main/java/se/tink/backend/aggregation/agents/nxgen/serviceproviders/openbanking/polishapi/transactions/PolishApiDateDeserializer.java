package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PolishApiDateDeserializer extends StdDeserializer<LocalDate> {

    private static final int ISO_DATE_LENGTH = 10;

    private PolishApiDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return LocalDate.parse(
                jsonParser.readValueAs(String.class).substring(0, ISO_DATE_LENGTH),
                DateTimeFormatter.ISO_DATE);
    }
}
