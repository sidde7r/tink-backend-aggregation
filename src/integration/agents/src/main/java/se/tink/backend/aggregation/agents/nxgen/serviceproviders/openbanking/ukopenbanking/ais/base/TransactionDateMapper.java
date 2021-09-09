package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.chrono.AvailableDateInformation;

@Slf4j
public class TransactionDateMapper {

    public static AvailableDateInformation prepareBookingDate(Instant bookingDateTime) {
        log.info("Instant bookingDateTime is {}", bookingDateTime);
        return new AvailableDateInformation()
                .setDate(LocalDateTime.ofInstant(bookingDateTime, ZoneOffset.UTC).toLocalDate())
                .setInstant(handleInstantWithDefaultTime(bookingDateTime));
    }

    private static Instant handleInstantWithDefaultTime(Instant bookingDateTime) {
        LocalDate localDate = bookingDateTime.atZone(ZoneOffset.UTC).toLocalDate();
        Instant instantWithDefaultTime = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        if (instantWithDefaultTime.equals(bookingDateTime)) {
            return null;
        }
        return bookingDateTime;
    }
}
