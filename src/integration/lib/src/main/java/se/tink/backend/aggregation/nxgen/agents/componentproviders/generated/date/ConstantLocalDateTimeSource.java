package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class ConstantLocalDateTimeSource implements LocalDateTimeSource {

    private static final Instant CONSTANT_INSTANT =
            LocalDate.of(1992, 4, 10).atStartOfDay(ZoneOffset.UTC).toInstant();

    @Override
    public LocalDateTime now() {
        return LocalDateTime.ofInstant(getInstant(), ZoneOffset.UTC);
    }

    @Override
    public Instant getInstant() {
        return CONSTANT_INSTANT;
    }
}
