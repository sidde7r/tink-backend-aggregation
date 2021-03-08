package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;

public class LansforsakringarDateUtilTest {

    @Test
    public void testGetCurrentOrNextBusinessDate_isMovedToNextBusinessDate() {
        // when
        LocalDate localDate = LocalDate.of(2020, 3, 22);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(localDate);

        // then
        assertEquals(currentOrNextBusinessDate, LocalDate.of(2020, 3, 23));
    }

    @Test
    public void testGetCurrentOrNextBusinessDate_isNotMovedToNextBusinessDate() {
        // when
        LocalDate from = LocalDate.of(2020, 3, 23);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(from);

        // then
        assertEquals(currentOrNextBusinessDate, LocalDate.of(2020, 3, 23));
    }
}
