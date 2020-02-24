package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class ConstantLocalDateTimeSource implements LocalDateTimeSource {

    private static final long CONSTANT_EPOCH_SECONDS = 702864000; // 10-04-1992 00:00:00 (UTC)

    @Override
    public LocalDateTime now() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(CONSTANT_EPOCH_SECONDS), ZoneId.of("UTC"));
    }
}
