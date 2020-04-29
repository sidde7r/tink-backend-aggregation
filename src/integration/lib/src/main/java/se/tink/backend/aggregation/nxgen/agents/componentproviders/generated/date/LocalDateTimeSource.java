package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Instant;
import java.time.LocalDateTime;

public interface LocalDateTimeSource {
    LocalDateTime now();

    Instant getInstant();
}
