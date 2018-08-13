package se.tink.backend.aggregation.agents.demo;

import java.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DemoDataUtilsTest {
    @Test
    public void dateToComingSundayInSameMonthShouldReturnCorrectNumOfDays() {
        final LocalDate date = LocalDate.of(2018, 8, 13);
        int daysToSunday = DemoDataUtils.daysToSunday(date);

        assertThat(daysToSunday).isEqualTo(6);
    }

    @Test
    public void dateToComingSundayInNextYearShouldReturnCorrectNumOfDays() {
        final LocalDate date = LocalDate.of(2016, 12, 29);
        int daysToSunday = DemoDataUtils.daysToSunday(date);

        assertThat(daysToSunday).isEqualTo(3);
    }
}
