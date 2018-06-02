package se.tink.backend.common.statistics;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.junit.Assert;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Category;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.main.TestUtils;

public abstract class StatisticGeneratorUnitTest {
    private final static String errorMsg = "Incorrect statistic%s. Expected: %s. Actual: %s";
    protected List<Category> incomeCategories;
    protected List<Category> expensesCategories;
    protected List<Category> transfersCategories;
    protected List<Category> excludeCategories;

    static List<Statistic> unwrapAndFilter(String type, List<Future<List<Statistic>>> futures) {
        return futures
                .stream()
                .map(StatisticsGenerator::unwrapStatisticsFuture)
                .flatMap(List::stream)
                .filter(s -> type.equals(s.getType()))
                .collect(Collectors.toList());
    }

    protected <T extends Comparable<T>> void assertSizeFormated(String msg, T actValue, T expValue) {
        Assert.assertEquals(String.format(errorMsg, " size" + msg, expValue, actValue), expValue, actValue);
    }

    protected <T extends Comparable<T>> void assertValueFormated(String msg, T actValue, T expValue) {
        Assert.assertEquals("Incorrect statistic value" + msg + ". Expected: " + expValue + ". Actual: " + actValue, expValue, actValue);
    }

    protected void assertResolutionTypeToSize(List<Statistic> statistics, int dailySize,  int weeklySize, int monthlySize, int monthlyAdjSize, int yearlySize) {
        Map<ResolutionTypes, Integer> resolutionTypeToSize = getCountByResolutionType(statistics);
        int resolutionTypeNum = 0;

        if (dailySize != 0) {
            Assert.assertTrue(resolutionTypeToSize.containsKey(ResolutionTypes.DAILY));
            assertSizeFormated(" for DAILY resolution type", resolutionTypeToSize.get(ResolutionTypes.DAILY), dailySize);
            resolutionTypeNum++;
        }
        if (weeklySize != 0) {
            Assert.assertTrue(resolutionTypeToSize.containsKey(ResolutionTypes.WEEKLY));
            assertSizeFormated(" for WEEKLY resolution type", resolutionTypeToSize.get(ResolutionTypes.WEEKLY), weeklySize);
            resolutionTypeNum++;
        }
        if (monthlySize != 0) {
            Assert.assertTrue(resolutionTypeToSize.containsKey(ResolutionTypes.MONTHLY));
            assertSizeFormated(" for MONTHLY resolution type", resolutionTypeToSize.get(ResolutionTypes.MONTHLY), monthlySize);
            resolutionTypeNum++;
        }
        if (monthlyAdjSize != 0) {
            Assert.assertTrue(resolutionTypeToSize.containsKey(ResolutionTypes.MONTHLY_ADJUSTED));
            assertSizeFormated(" for MONTHLY_ADJUSTED resolution type", resolutionTypeToSize.get(ResolutionTypes.MONTHLY_ADJUSTED), monthlyAdjSize);
            resolutionTypeNum++;
        }
        if (yearlySize != 0) {
            Assert.assertTrue(resolutionTypeToSize.containsKey(ResolutionTypes.YEARLY));
            assertSizeFormated(" for YEARLY resolution type", resolutionTypeToSize.get(ResolutionTypes.YEARLY), yearlySize);
            resolutionTypeNum++;
        }

        Assert.assertEquals(resolutionTypeNum, resolutionTypeToSize.size());

    }

    protected void assertValue(List<Statistic> statistics, double daily, double weekly, double monthly,
            double monthlyAdj, double yearly) {
        for (Statistic statistic : statistics) {
            double expValue;
            switch (statistic.getResolution()) {
            case DAILY:
                expValue = daily;
                break;
            case WEEKLY:
                expValue = weekly;
                break;
            case MONTHLY:
                expValue = monthly;
                break;
            case MONTHLY_ADJUSTED:
                expValue = monthlyAdj;
                break;
            case YEARLY:
                expValue = yearly;
                break;
            default:
                expValue = 0;
            }

            assertValueFormated( " for resolution " + statistic.getResolution(), statistic.getValue(), expValue);
        }
    }

    protected Map<ResolutionTypes, Integer> getCountByResolutionType(List<Statistic> statistics) {
        Map<ResolutionTypes, Integer> typeToSize = Maps.newHashMap();

        for (Statistic statistic : statistics) {
            ResolutionTypes type = statistic.getResolution();
            int size = 0;
            if (typeToSize.containsKey(type)) {
                size = typeToSize.get(type);
            }
            size++;
            typeToSize.put(type, size);
        }

        return typeToSize;
    }

    protected List<Statistic> filter(List<Statistic> statistics, final String statisticType){
        return Lists.newArrayList(Iterables.filter(statistics, statistic -> statistic.getType().equals(statisticType)));
    }

    protected List<Category> generateCategories() {
        incomeCategories = Lists.newArrayList(TestUtils.createCategory(SECategories.Codes.INCOME_SALARY_OTHER),
                TestUtils.createCategory(SECategories.Codes.INCOME_BENEFITS_OTHER));
        expensesCategories = Lists.newArrayList( TestUtils.createCategory(SECategories.Codes.EXPENSES_FOOD_COFFEE),
                TestUtils.createCategory(SECategories.Codes.EXPENSES_HOME),
                TestUtils.createCategory(SECategories.Codes.EXPENSES_SHOPPING));
        transfersCategories = Lists.newArrayList(TestUtils.createCategory(SECategories.Codes.TRANSFERS_SAVINGS));

        excludeCategories = Lists.newArrayList(TestUtils.createCategory(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED),
                TestUtils.createCategory(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER),
                TestUtils.createCategory(SECategories.Codes.TRANSFERS_SAVINGS_OTHER));

        List<Category> testCategories = Lists.newArrayList();

        testCategories.addAll(incomeCategories);
        testCategories.addAll(expensesCategories);
        testCategories.addAll(transfersCategories);
        testCategories.addAll(excludeCategories);

        return testCategories;
    }

    protected List<Statistic> unwrap(List<Future<List<Statistic>>> futures) {
        return futures
                .stream()
                .map(StatisticsGenerator::unwrapStatisticsFuture)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
