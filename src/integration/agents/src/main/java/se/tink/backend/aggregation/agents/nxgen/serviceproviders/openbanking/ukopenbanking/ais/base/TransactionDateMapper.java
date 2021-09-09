package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionDateMapper {

    public static AvailableDateInformation prepareTransactionDate(Instant transactionDateTime) {
        return new AvailableDateInformation()
                .setDate(LocalDateTime.ofInstant(transactionDateTime, ZoneOffset.UTC).toLocalDate())
                .setInstant(handleInstantWithDefaultTime(transactionDateTime));
    }

    private static Instant handleInstantWithDefaultTime(Instant transactionDateTime) {
        LocalDate localDate = transactionDateTime.atZone(ZoneOffset.UTC).toLocalDate();
        Instant instantWithDefaultTime = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        if (verifyWinterUkDefaultTime(transactionDateTime, instantWithDefaultTime)
                || verifySummerUkDefaultTime(transactionDateTime, instantWithDefaultTime)) {
            return null;
        }
        return transactionDateTime;
    }

    private static boolean verifyWinterUkDefaultTime(
            Instant bookingDateTime, Instant instantWithDefaultTime) {
        return instantWithDefaultTime.equals(bookingDateTime);
    }

    private static boolean verifySummerUkDefaultTime(
            Instant bookingDateTime, Instant instantWithDefaultTime) {
        return instantWithDefaultTime.plus(23, ChronoUnit.HOURS).equals(bookingDateTime);
    }
}
