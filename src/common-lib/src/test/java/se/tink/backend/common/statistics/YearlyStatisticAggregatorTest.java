package se.tink.backend.common.statistics;

import org.junit.Test;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Statistic.Types;
import se.tink.libraries.date.ResolutionTypes;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.common.statistics.YearlyStatisticAggregator.transformMonthlyToYearly;

public class YearlyStatisticAggregatorTest {

    @Test
    public void transformToYearly() {
        String statisticType = Types.EXPENSES_BY_CATEGORY;
        assertTrue(YearlyStatisticAggregator.ENABLED_TYPES.contains(statisticType));

        Statistic marchCategory1 = new Statistic();
        marchCategory1.setValue(300);
        marchCategory1.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        marchCategory1.setType(statisticType);
        marchCategory1.setPeriod("2017-03");
        marchCategory1.setDescription("category1");
        Statistic marchCategory2 = new Statistic();
        marchCategory2.setValue(400);
        marchCategory2.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        marchCategory2.setType(statisticType);
        marchCategory2.setPeriod("2017-03");
        marchCategory2.setDescription("category2");
        Statistic aprilCategory1 = new Statistic();
        aprilCategory1.setValue(200);
        aprilCategory1.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        aprilCategory1.setType(statisticType);
        aprilCategory1.setPeriod("2017-04");
        aprilCategory1.setDescription("category1");

        Statistic yearlyCategory1 = new Statistic();
        yearlyCategory1.setValue(500);
        yearlyCategory1.setResolution(ResolutionTypes.YEARLY);
        yearlyCategory1.setType(statisticType);
        yearlyCategory1.setPeriod("2017");
        yearlyCategory1.setDescription("category1");
        Statistic yearlyCategory2 = new Statistic();
        yearlyCategory2.setValue(400);
        yearlyCategory2.setResolution(ResolutionTypes.YEARLY);
        yearlyCategory2.setType(statisticType);
        yearlyCategory2.setPeriod("2017");
        yearlyCategory2.setDescription("category2");

        assertThat(
                transformMonthlyToYearly(asList(marchCategory1, marchCategory2, aprilCategory1)),
                hasItems(yearlyCategory1, yearlyCategory2));
    }

    @Test
    public void transformToYearlyWhenNoStatistics() {
        assertTrue(transformMonthlyToYearly(emptyList()).isEmpty());
    }

    @Test
    public void transformToYearlyWhenNoMonthly() {
        String statisticType = Types.INCOME_AND_EXPENSES;
        assertTrue(YearlyStatisticAggregator.ENABLED_TYPES.contains(statisticType));

        Statistic dailyStatistic = new Statistic();
        dailyStatistic.setResolution(ResolutionTypes.DAILY);
        dailyStatistic.setType(statisticType);
        Statistic weeklyStatistic = new Statistic();
        weeklyStatistic.setResolution(ResolutionTypes.WEEKLY);
        weeklyStatistic.setType(statisticType);

        assertTrue(transformMonthlyToYearly(asList(dailyStatistic, weeklyStatistic)).isEmpty());
    }

    @Test
    public void transformToYearlyWhenNoEnabledTypes() {
        String statisticType = Types.LEFT_TO_SPEND_AVERAGE;
        assertFalse(YearlyStatisticAggregator.ENABLED_TYPES.contains(statisticType));

        Statistic monthlyStatistic1 = new Statistic();
        monthlyStatistic1.setResolution(ResolutionTypes.MONTHLY);
        monthlyStatistic1.setType(statisticType);
        Statistic monthlyStatistic2 = new Statistic();
        monthlyStatistic2.setResolution(ResolutionTypes.MONTHLY);
        monthlyStatistic2.setType(statisticType);

        assertTrue(transformMonthlyToYearly(asList(monthlyStatistic1, monthlyStatistic2)).isEmpty());
    }

}
