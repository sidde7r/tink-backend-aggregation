package se.tink.backend.system.workers.processor.loan;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class InterestRateDaysTest {

    static SimpleDateFormat YEAR = new SimpleDateFormat("yyyy");

    @Test
    public void shouldFailIfDaysHasntBeenFilledOut() {
        Date today = new Date();
        try {
            InterestRateDays.DanskeBank.getMortgageInterestRateDays(today);
        } catch (Exception e) {
            fail("If this starts to fail, it's most likely due to that it's a new year and we haven't filled out "
                    + "InterestRateDays.DanskeBank days for " + YEAR.format(today) + " yet!");
        }
    }

    @Test
    public void testMortgageCorrectDaysFor2016() {

        Calendar c = Calendar.getInstance();

        c.set(2016, 1, 1);
        assertEquals(31, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2016, 1, 29);
        assertEquals(28, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2016, 4, 31);
        assertEquals(29, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2016, 7, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2016, 9, 31);
        assertEquals(31, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2016, 11, 30);
        assertEquals(30, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));
    }

    @Test
    public void testBlancoCorrectDaysFor2016() {

        Calendar c = Calendar.getInstance();

        c.set(2016, 0, 31);
        assertEquals(33, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2016, 1, 31);
        assertEquals(28, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2016, 4, 31);
        assertEquals(29, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2016, 7, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2016, 9, 31);
        assertEquals(31, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2016, 11, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));
    }

    @Test
    public void testMortgageCorrectDaysFor2015() {

        Calendar c = Calendar.getInstance();

        c.set(2015, 0, 31);
        assertEquals(32, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2015, 1, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2015, 5, 1);
        assertEquals(31, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2015, 7, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2015, 9, 31);
        assertEquals(32, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));

        c.set(2015, 11, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getMortgageInterestRateDays(c.getTime()));
    }

    @Test
    public void testBlancoCorrectDaysFor2015() {

        Calendar c = Calendar.getInstance();

        c.set(2015, 0, 31);
        assertEquals(34, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2015, 1, 31);
        assertEquals(28, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2015, 5, 1);
        assertEquals(32, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2015, 7, 31);
        assertEquals(31, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2015, 9, 31);
        assertEquals(33, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

        c.set(2015, 11, 31);
        assertEquals(30, InterestRateDays.DanskeBank.getBlancoInterestRateDays(c.getTime()));

    }
}
