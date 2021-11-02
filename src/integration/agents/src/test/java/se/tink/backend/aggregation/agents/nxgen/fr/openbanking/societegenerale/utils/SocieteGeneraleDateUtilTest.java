package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils;

import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.libraries.payment.rpc.Payment;

public class SocieteGeneraleDateUtilTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");

    private Clock fixedClock(Calendar calendar) {
        final Instant instant = calendar.toInstant();
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnANonBusinessDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 10, 0, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));
        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(false);

        Assertions.assertThat(SocieteGeneraleDateUtil.getExecutionDate(payment))
                .contains("2016-02-15");
    }

    @Test
    public void testExecutionDateIsNotMovedWhenSetOnABusinessDayBeforeCutoff() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 10, 0, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));
        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(false);

        Assertions.assertThat(SocieteGeneraleDateUtil.getExecutionDate(payment))
                .contains("2016-02-15");
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnABusinessDayAfterCutoff() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 17, 16, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));
        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(false);

        Assertions.assertThat(SocieteGeneraleDateUtil.getExecutionDate(payment))
                .contains("2016-02-16");
    }

    @Test
    public void shouldReturnSameDateAsPaymentExecutionDate() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 17, 16, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));

        LocalDate executionDate = LocalDate.of(2021, 1, 1);
        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(true);
        given(payment.getExecutionDate()).willReturn(executionDate);

        // when
        String result = SocieteGeneraleDateUtil.getExecutionDate(payment);

        // then
        Assertions.assertThat(result).contains("2021-01-01");
    }

    @Test
    public void shouldReturnTodayDateAsPaymentExecutionDateWhenNoExecutionDateIsSet() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 17, 16, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));

        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(true);
        given(payment.getExecutionDate()).willReturn(null);

        // when
        String result = SocieteGeneraleDateUtil.getExecutionDate(payment);

        // then
        Assertions.assertThat(result).contains("2016-02-15");
    }
}
