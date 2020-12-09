package se.tink.libraries.chrono;

import java.time.Instant;
import java.time.LocalDate;

public class AvailableDateInformation {

    private LocalDate date;
    /*
    Field should be set only if time is provided
     */
    private Instant instant;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }
}
