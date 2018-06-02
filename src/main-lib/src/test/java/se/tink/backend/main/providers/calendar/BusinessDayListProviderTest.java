package se.tink.backend.main.providers.calendar;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BusinessDayListProviderTest {
    @Test
    public void hasCorrectBusinessDaysInJune2016() {
        BusinessDayListProvider businessDayListProvider = new BusinessDayListProvider();

        Map<String, Map<String, List<Integer>>> businessDayList = businessDayListProvider
                .listBusinessDays(new LocalDate(2016, 6, 1), 1);

        List<Integer> juneBusinessDays = businessDayList.get("2016").get("6");

        ImmutableList<Integer> expectedJuneBusinessDays = ImmutableList.of(
                1,2,3,
                7,8,9,10,
                13,14,15,16,17,
                20,21,22,23,
                27,28,29,30);
        assertThat(juneBusinessDays).hasSameSizeAs(expectedJuneBusinessDays);
        assertThat(juneBusinessDays).containsAll(expectedJuneBusinessDays);
    }

    @Test
    public void returnsSeveralMonthsInSameYear() {
        BusinessDayListProvider businessDayListProvider = new BusinessDayListProvider();

        Map<String, Map<String, List<Integer>>> businessDayList = businessDayListProvider
                .listBusinessDays(new LocalDate(2016, 2, 1), 3);

        Map<String, List<Integer>> businessDays2016 = businessDayList.get("2016");

        assertThat(businessDays2016).hasSize(3);
        assertThat(businessDays2016).containsKeys("2", "3", "3");
        for (List<Integer> daysInMonth : businessDays2016.values()) {
            assertThat(daysInMonth).isNotEmpty();
        }
    }

    @Test
    public void returnsMultipleYearsForMonthsSpanningOverMultipleYears() {
        BusinessDayListProvider businessDayListProvider = new BusinessDayListProvider();

        Map<String, Map<String, List<Integer>>> businessDayList = businessDayListProvider
                .listBusinessDays(new LocalDate(2016, 12, 1), 2);

        assertThat(businessDayList).hasSize(2);
        assertThat(businessDayList).containsKeys("2016", "2017");
        for (Map<String, List<Integer>> months : businessDayList.values()) {
            assertThat(months).isNotEmpty();
        }
    }
}