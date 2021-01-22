package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ISOInstantDeserializer extends InstantDeserializer<Instant> {

    public ISOInstantDeserializer() {
        super(INSTANT, DateTimeFormatter.ISO_DATE_TIME);
    }
}
