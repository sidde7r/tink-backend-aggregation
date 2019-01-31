package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;

public class FinTsDateParserTest {
    private HashMap<String, LocalDate> testData = new HashMap<String, LocalDate>() {{
        put("1812241219D8", LocalDate.of(2018, 12,19));
        put("1812191219D10", LocalDate.of(2018, 12, 19));
        put("1812201220D0", LocalDate.of(2018, 12, 20));
        put("1812211221D100", LocalDate.of(2018, 12, 21));
        put("1812241224C90", LocalDate.of(2018, 12, 24));
        put("1812271227D33", LocalDate.of(2018, 12, 27));
        put("1812271227D41", LocalDate.of(2018, 12, 27));
        put("1901031227D11", LocalDate.of(2018, 12, 27));
        put("1901031227D10", LocalDate.of(2018, 12, 27));
        put("1812281228C100", LocalDate.of(2018, 12, 28));
        put("1901020102C50", LocalDate.of(2019, 1, 2));
        put("1901020102D19", LocalDate.of(2019, 1, 2));
        put("1901020102D23", LocalDate.of(2019, 1, 2));
        put("1901070102D28", LocalDate.of(2019, 1, 2));
        put("1901070102D7", LocalDate.of(2019, 1, 2));
        put("1901080103D2", LocalDate.of(2019, 1, 3));
        put("1901080103D1", LocalDate.of(2019, 1, 3));
        put("1901040104D637", LocalDate.of(2019, 1, 4));
        put("1901090104D14", LocalDate.of(2019, 1, 4));
        put("1901090104D2", LocalDate.of(2019, 1, 4));
        put("1901090104D4", LocalDate.of(2019, 1, 4));
        put("1901070107C85", LocalDate.of(2019, 1, 7));
        put("1901070107D85", LocalDate.of(2019, 1, 7));
        put("1901080108C191", LocalDate.of(2019, 1, 8));
        put("1901170117D265", LocalDate.of(2019, 1, 17));
        put("1901220117D0", LocalDate.of(2019, 1, 17));
        put("1901150121D17", LocalDate.of(2019, 1, 21));
        put("1901180124D1", LocalDate.of(2019, 1, 24));
        put("1901180124D37", LocalDate.of(2019, 1, 24));
        put("190118D37", LocalDate.of(2019, 1, 18));
        put("19011811D37", LocalDate.of(2019, 1, 18));
        put("180229D37", LocalDate.of(2018, 2, 28));
    }};

    @Test
    public void testFinTsDateParser() {
        testData.forEach(this::testDateParse);
    }

    private void testDateParse(final String key, final LocalDate value) {
        Assert.assertEquals(FinTsDateParser.parseDate(key), value);
    }
}
