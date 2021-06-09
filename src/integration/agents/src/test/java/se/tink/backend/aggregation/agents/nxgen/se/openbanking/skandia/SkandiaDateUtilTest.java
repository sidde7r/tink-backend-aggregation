package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.DOMESTIC_CUT_OFF_HOURS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.DOMESTIC_CUT_OFF_MINUTES;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.GIROS_DOMESTIC_CUT_OFF_HOURS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.GIROS_DOMESTIC_CUT_OFF_MINUTES;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import org.junit.Test;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class SkandiaDateUtilTest {

    private void setClock(Calendar calendar) {
        SkandiaDateUtil.setClockForTesting(
                Clock.fixed(calendar.toInstant(), ZoneId.of("Europe/Stockholm")));
    }

    @Test
    public void
            testExecutionDateIsNotMovedToNextBusinessDayWhenSetForABusinessDateAndIntraBankForDomestic() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.FEBRUARY, 9, 20, 0, 0);
        setClock(calendar);

        Payment payment = mock(Payment.class);

        when(payment.getCreditor()).thenReturn(new Creditor(new SwedishIdentifier("91501111111")));
        when(payment.getDebtor()).thenReturn(new Debtor(new SwedishIdentifier("91511111111")));

        assertThat(SkandiaDateUtil.getExecutionDate(payment).toString()).isEqualTo("2021-02-09");
    }

    @Test
    public void
            testExecutionDateIsNotMovedToNextBusinessDayWhenSetForAWeekendDateAndIntraBankForDomestic() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.FEBRUARY, 6, 20, 0, 0);
        setClock(calendar);

        Payment payment = mock(Payment.class);

        when(payment.getCreditor()).thenReturn(new Creditor(new SwedishIdentifier("91501111111")));
        when(payment.getDebtor()).thenReturn(new Debtor(new SwedishIdentifier("91511111111")));

        when(payment.getExecutionDate()).thenReturn(LocalDate.of(2021, 2, 6));

        assertThat(SkandiaDateUtil.getExecutionDate(payment).toString()).isEqualTo("2021-02-06");
    }

    @Test
    public void
            testExecutionDateIsMovedToNextBusinessDayWhenSetAfterCutOffAndInterBankForDomestic() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                2021,
                Calendar.FEBRUARY,
                9,
                DOMESTIC_CUT_OFF_HOURS,
                DOMESTIC_CUT_OFF_MINUTES + 1,
                0);

        setClock(calendar);
        Payment payment = mock(Payment.class);

        when(payment.getExecutionDate()).thenReturn(null);

        when(payment.getCreditor()).thenReturn(new Creditor(new SwedishIdentifier("10001111111")));
        when(payment.getDebtor()).thenReturn(new Debtor(new SwedishIdentifier("91511111111")));

        assertThat(SkandiaDateUtil.getExecutionDate(payment).toString()).isEqualTo("2021-02-10");
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetAfterCutOffForGirosDomestic() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                2021,
                Calendar.FEBRUARY,
                9,
                GIROS_DOMESTIC_CUT_OFF_HOURS,
                GIROS_DOMESTIC_CUT_OFF_MINUTES + 1,
                0);

        setClock(calendar);
        Payment payment = mock(Payment.class);

        when(payment.getCreditor()).thenReturn(new Creditor(new BankGiroIdentifier("1111111")));
        when(payment.getExecutionDate()).thenReturn(null);

        assertThat(SkandiaDateUtil.getExecutionDate(payment).toString()).isEqualTo("2021-02-10");
    }

    @Test
    public void testExecutionDateIsNotMovedToNextBusinessDayWhenSetBeforeCutOffForGirosDomestic() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                2021,
                Calendar.FEBRUARY,
                9,
                GIROS_DOMESTIC_CUT_OFF_HOURS - 1,
                GIROS_DOMESTIC_CUT_OFF_MINUTES,
                0);

        setClock(calendar);

        Payment payment = mock(Payment.class);

        when(payment.getCreditor()).thenReturn(new Creditor(new BankGiroIdentifier("1111111")));

        when(payment.getExecutionDate()).thenReturn(null);

        assertThat(SkandiaDateUtil.getExecutionDate(payment).toString()).isEqualTo("2021-02-09");
    }
}
