package se.tink.backend.main.utils;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import static org.assertj.core.api.Assertions.assertThat;

public class FollowItemUtilsTest {
    @Test
    public void testMergeEmptyList() {
        Map<Period, Double> result = FollowItemUtils.mergeByPeriod(getProfile(), Collections.emptyList());

        assertThat(result.keySet().isEmpty()).isTrue();
    }

    @Test
    public void testOneItem() {
        Statistic statistic = createStatistics("2018-01-01", 100D);

        Map<Period, Double> result = FollowItemUtils.mergeByPeriod(getProfile(), ImmutableList.of(statistic));

        assertThat(result.entrySet().size()).isEqualTo(1);
    }

    @Test
    public void testTwoItemsOnDifferentDates() {
        Statistic statistic1 = createStatistics("2018-01-01", 100D);
        Statistic statistic2 = createStatistics("2018-01-02", 100D);

        Map<Period, Double> result = FollowItemUtils
                .mergeByPeriod(getProfile(), ImmutableList.of(statistic1, statistic2));

        assertThat(result.entrySet().size()).isEqualTo(2);
    }

    @Test
    public void testTwoItemsOnSameDate() {
        Statistic statistic1 = createStatistics("2018-01-01", 100D);
        Statistic statistic2 = createStatistics("2018-01-02", 100D);
        Statistic statistic3 = createStatistics("2018-01-01", 100D);

        Map<Period, Double> result = FollowItemUtils
                .mergeByPeriod(getProfile(), ImmutableList.of(statistic1, statistic2, statistic3));

        assertThat(result.entrySet().size()).isEqualTo(2);
        assertThat(result.get(DateUtils.buildDailyPeriod("2018-01-01"))).isEqualTo(200D);
    }

    private static UserProfile getProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        userProfile.setPeriodAdjustedDay(25);
        return userProfile;
    }

    private static Statistic createStatistics(String date, double value) {
        Statistic statistic = new Statistic();
        statistic.setPeriod(date);
        statistic.setValue(value);
        statistic.setResolution(ResolutionTypes.DAILY);

        return statistic;
    }
}
