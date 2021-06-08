package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class FrOpenBankingDateUtilTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");

    private Clock fixedClock(Calendar calendar) {
        final Instant instant = calendar.toInstant();
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnANonBusinessDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 14);
        FrOpenBankingDateUtil.setClock(fixedClock(cal));

        Assertions.assertThat(FrOpenBankingDateUtil.getExecutionDate(null)).isEqualTo("2016-02-15");
    }

    @Test
    public void testExecutionDateIsNotMovedWhenSetOnABusinessDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15);
        FrOpenBankingDateUtil.setClock(fixedClock(cal));

        Assertions.assertThat(FrOpenBankingDateUtil.getExecutionDate(null)).isEqualTo("2016-02-15");
    }
}
