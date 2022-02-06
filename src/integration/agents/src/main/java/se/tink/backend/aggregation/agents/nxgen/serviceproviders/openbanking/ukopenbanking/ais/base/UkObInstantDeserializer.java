package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Handles deserialization of date time string into Instant
 *
 * <p>for all known cases that UK banks provide
 */
public class UkObInstantDeserializer extends InstantDeserializer<Instant> {

    public UkObInstantDeserializer() {
        super(
                INSTANT,
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm[:ss]")
                        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                        .appendPattern("[VV][xxx][xx][x]")
                        .toFormatter()
                        .withZone(ZoneOffset.UTC));
    }
}
