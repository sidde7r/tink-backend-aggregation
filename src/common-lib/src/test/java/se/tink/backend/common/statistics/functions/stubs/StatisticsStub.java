package se.tink.backend.common.statistics.functions.stubs;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;

public class StatisticsStub {
    public static final String USER_ID = "userid";
    public static final String LEFT_TO_SPEND_AVERAGE = "left-to-spend-average";
    private static final String DECEMBER_2014 = "2014-12";
    private static final String JANUARY_2015 = "2015-01";
    private static final String FEBRUARY_2015 = "2015-02";
    private static final String MARCH_2015 = "2015-03";

    public static Statistic createLTSA_Daily(String period, String description, double value) {
        Statistic statistic = new Statistic();
        statistic.setUserId(USER_ID);
        statistic.setType(LEFT_TO_SPEND_AVERAGE);
        statistic.setPayload(null);

        statistic.setResolution(ResolutionTypes.DAILY);

        statistic.setValue(value);
        statistic.setPeriod(period);
        statistic.setDescription(description);

        return statistic;
    }

    public static Statistic createLTSA_Monthly(String period, String description, double value, String userId, boolean monthlyAdjusted) {
        Statistic statistic = new Statistic();
        statistic.setUserId(userId);
        statistic.setType(LEFT_TO_SPEND_AVERAGE);
        statistic.setPayload(null);

        if (monthlyAdjusted) {
            statistic.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        } else {
            statistic.setResolution(ResolutionTypes.MONTHLY);
        }

        statistic.setValue(value);
        statistic.setPeriod(period);
        statistic.setDescription(description);

        return statistic;
    }

    public static Statistic createLTSA_MonthlyJanuary10th10valued() {
        return createLTSA_MonthlyJanuary("10", 10.0);
    }

    public static Statistic createLTSA_MonthlyDecember(String day, double value) {
        return createLTSA(DECEMBER_2014, "2014", "12", day, value);
    }

    public static Statistic createLTSA_MonthlyJanuary(String day, Double value) {
        return createLTSA_JanuaryPeriod("01", day, value);
    }

    public static Statistic createLTSA_MonthlyFebruary(String day, Double value) {
        return createLTSA_FebruaryPeriod("02", day, value);
    }

    public static Statistic createLTSA_MonthlyMarch(String day, Double value) {
        return createLTSA(MARCH_2015, "2015", "03", day, value);
    }

    public static Statistic createLTSA_JanuaryPeriod(String month, String day, Double value) {
        return createLTSA(JANUARY_2015, "2015", month, day, value);
    }

    public static Statistic createLTSA_FebruaryPeriod(String month, String day, Double value) {
        return createLTSA(FEBRUARY_2015, "2015", month, day, value);
    }
    public static Statistic createLTSA(int year, int month, int day, Double value) {
        String monthStr = ((month < 10) ? "0" : "") + month;
        String dayStr = ((day < 10) ? "0" : "") + day;
        return createLTSA(year + "-" + monthStr, String.valueOf(year), String.valueOf(monthStr), String.valueOf(dayStr),
                value);
    }

    public static Statistic createLTSA(String period, String year, String month, String day, Double value) {
        Statistic statistic = new Statistic();
        statistic.setDescription(year + "-" + month + "-" + day);
        statistic.setPayload(null);
        statistic.setPeriod(period);
        statistic.setResolution(ResolutionTypes.DAILY);
        statistic.setType(LEFT_TO_SPEND_AVERAGE);
        statistic.setUserId(USER_ID);
        statistic.setValue(value);
        return statistic;
    }

    public static Builder stubBuilder() {
        return new Builder();
    }

    public static List<Statistic> stubOf(Statistic statistic) {
        return ImmutableList.of(statistic);
    }

    public static class Builder {
        private final List<Statistic> statistics;

        private Builder () {
            this.statistics = Lists.newArrayList();
        }

        public Builder add(Statistic statistic) {
            this.statistics.add(statistic);
            return this;
        }

        public List<Statistic> toList() {
            return statistics;
        }
    }
}
