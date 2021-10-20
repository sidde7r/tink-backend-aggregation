package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UpcomingPaymentEntityTest {
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final Locale LOCALE = new Locale("sv", "SE");
    private static final ThreadSafeDateFormat FORMATTER_MILLISECONDS_WITH_TIMEZONE =
            new ThreadSafeDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", LOCALE, TimeZone.getTimeZone(ZONE_ID));

    @Test
    public void shouldReturnIsSamePaymentTrueWhenSameDateAndOffsetIsPlusTwo() throws Exception {
        // given
        final Date paymentRequestDate =
                FORMATTER_MILLISECONDS_WITH_TIMEZONE.parse("2021-10-29T00:00:00.000+0200");
        final UpcomingPaymentEntity upcomingPaymentEntity =
                getUpcomingPaymentEntityWithOnlyDateOffsetPlusTwo();

        // when
        final boolean hasSameDate = upcomingPaymentEntity.hasSameDate(paymentRequestDate);

        // then
        assertTrue(hasSameDate);
    }

    @Test
    public void shouldReturnIsSamePaymentTrueWhenSameDateAndOffsetIsPlusOne() throws Exception {
        // given
        final Date paymentRequestDate =
                FORMATTER_MILLISECONDS_WITH_TIMEZONE.parse("2021-11-26T00:00:00.000+0100");
        final UpcomingPaymentEntity upcomingPaymentEntity =
                getUpcomingPaymentEntityWithOnlyDateOffsetPlusOne();

        // when
        final boolean hasSameDate = upcomingPaymentEntity.hasSameDate(paymentRequestDate);

        // then
        assertTrue(hasSameDate);
    }

    @Test
    public void shouldReturnIsSamePaymentFalseIfDatesAreDifferent() throws Exception {
        // given
        final Date paymentRequestDate =
                FORMATTER_MILLISECONDS_WITH_TIMEZONE.parse("2021-10-30T00:00:00.000+0200");
        final UpcomingPaymentEntity upcomingPaymentEntity =
                getUpcomingPaymentEntityWithOnlyDateOffsetPlusTwo();

        // when
        final boolean hasSameDate = upcomingPaymentEntity.hasSameDate(paymentRequestDate);

        // then
        assertFalse(hasSameDate);
    }

    private UpcomingPaymentEntity getUpcomingPaymentEntityWithOnlyDateOffsetPlusTwo() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "  \"Date\": \"2021-10-29T00:00:00+02:00\"\n" + "}",
                UpcomingPaymentEntity.class);
    }

    private UpcomingPaymentEntity getUpcomingPaymentEntityWithOnlyDateOffsetPlusOne() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "  \"Date\": \"2021-11-26T00:00:00+01:00\"\n" + "}",
                UpcomingPaymentEntity.class);
    }
}
