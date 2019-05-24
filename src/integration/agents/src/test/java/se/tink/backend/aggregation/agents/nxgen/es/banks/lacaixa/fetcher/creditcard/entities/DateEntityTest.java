package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

public class DateEntityTest {
    @Test
    public void testParseDate() throws ParseException {
        assertEquals(makeDate(2019, 5, 17), DateEntity.parseDate("17052019", "DDMMAAAA"));
        assertEquals(makeDate(2022, 12, 31), DateEntity.parseDate("31122022", "DDMMAAAA"));
    }

    private Date makeDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
