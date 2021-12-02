package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class SessionExpiryDateComparatorTest {

    @Test
    @Parameters
    public void shouldCompareSessionExpiryDate(Date sessionExpiryDate, String expected) {
        // when
        String result = SessionExpiryDateComparator.getSessionExpiryInfo(sessionExpiryDate);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldCompareSessionExpiryDate() {
        return new Object[][] {
            {null, null},
            {DateUtils.addHours(new Date(), 1), "true"},
            {DateUtils.addSeconds(new Date(), -1), "false"},
        };
    }
}
