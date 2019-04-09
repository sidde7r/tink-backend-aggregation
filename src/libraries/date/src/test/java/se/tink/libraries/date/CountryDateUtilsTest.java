package se.tink.libraries.date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.function.Consumer;
import org.junit.Test;

public class CountryDateUtilsTest {

    private final CountryDateUtils swedishUtils = CountryDateUtils.getSwedishDateUtils();
    private final CountryDateUtils belgianUtils = CountryDateUtils.getBelgianDateUtils();

    private static void testDayBoundaries(Consumer<Calendar> consumer) {
        Calendar c = DateUtils.getCalendar();
        c.set(Calendar.YEAR, 2018);

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        consumer.accept(c);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        consumer.accept(c);
    }

    @Test
    public void swedishIndependenceDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 5);
                    c.set(Calendar.DATE, 6);
                    assertFalse(c.toString(), swedishUtils.isBusinessDay(c));
                    assertTrue(c.toString(), belgianUtils.isBusinessDay(c));
                });
    }

    @Test
    public void christmasDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 25);
                    assertFalse(c.toString(), swedishUtils.isBusinessDay(c));
                    assertFalse(c.toString(), belgianUtils.isBusinessDay(c));
                });
    }

    @Test
    public void christmasEveTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 24);
                    assertFalse(c.toString(), swedishUtils.isBusinessDay(c));
                    assertTrue(c.toString(), belgianUtils.isBusinessDay(c));
                });
    }

    @Test
    public void saintsDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 10);
                    c.set(Calendar.DATE, 1);
                    assertTrue(c.toString(), swedishUtils.isBusinessDay(c));
                    assertFalse(c.toString(), belgianUtils.isBusinessDay(c));
                });
    }

    @Test
    public void kingsDay() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 3);
                    c.set(Calendar.DATE, 2);
                    assertFalse(c.toString(), swedishUtils.isBusinessDay(c));
                    assertFalse(c.toString(), belgianUtils.isBusinessDay(c));
                });
    }
}
