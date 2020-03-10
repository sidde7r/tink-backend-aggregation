package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import java.time.Instant;
import java.time.LocalDate;

public class DateTimeProvider {

    public Instant getInstantNow() {
        return Instant.now();
    }

    public LocalDate getDateNow() {
        return LocalDate.now();
    }
}
