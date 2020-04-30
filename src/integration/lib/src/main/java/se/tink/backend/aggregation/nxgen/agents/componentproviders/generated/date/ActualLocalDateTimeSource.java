package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Instant;
import java.time.LocalDateTime;

public final class ActualLocalDateTimeSource implements LocalDateTimeSource {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public Instant getInstant() {
        return Instant.now();
    }
}
