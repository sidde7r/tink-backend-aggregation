package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import org.assertj.core.api.Assertions;
import org.junit.Test;

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

        Assertions.assertThat(SocieteGeneraleDateUtil.getExecutionDate(null))
                .contains("2016-02-15");
    }

    @Test
    public void testExecutionDateIsNotMovedWhenSetOnABusinessDayBeforeCutoff() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 10, 0, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));

        Assertions.assertThat(SocieteGeneraleDateUtil.getExecutionDate(null))
                .contains("2016-02-15");
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnABusinessDayAfterCutoff() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 17, 16, 0);
        SocieteGeneraleDateUtil.setClock(fixedClock(cal));

        Assertions.assertThat(SocieteGeneraleDateUtil.getExecutionDate(null))
                .contains("2016-02-16");
    }
}
