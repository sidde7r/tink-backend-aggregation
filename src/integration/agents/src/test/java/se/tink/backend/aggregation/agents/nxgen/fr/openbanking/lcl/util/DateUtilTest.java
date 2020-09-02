package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DateUtilTest {

    @Test
    public void testTplusOneDate() {
        assertThat(DateUtil.plusOneDayDate("2016-12-30T00:00:00.000+01:00"))
                .isEqualTo("2016-12-31T00:00:00.000+01:00");
    }
}
