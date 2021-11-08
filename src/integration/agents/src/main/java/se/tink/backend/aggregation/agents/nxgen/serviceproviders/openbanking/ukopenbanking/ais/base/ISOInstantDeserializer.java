package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class ISOInstantDeserializer extends InstantDeserializer<Instant> {

    public ISOInstantDeserializer() {
        super(
                INSTANT,
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm[:ss]")
                        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                        .appendPattern("[VV][xxx][xx][x]")
                        .toFormatter());
    }
}
