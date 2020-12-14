package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantDeserializer extends StdDeserializer<Instant> {

    public InstantDeserializer() {
        this(null);
    }

    protected InstantDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String date = jsonParser.getText();
        return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(date));
    }
}
