package se.tink.backend.main.utils;

import java.util.List;
import java.util.Map;
import org.assertj.core.util.Maps;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.Period;

public class FollowItemUtils {
    public static Map<Period, Double> mergeByPeriod(UserProfile userProfile, List<Statistic> statistics) {

        Map<Period, Double> result = Maps.newHashMap();

        for (Statistic statistic : statistics) {
            Period period = UserProfile.ProfileDateUtils.buildPeriod(statistic.getPeriod(), statistic.getResolution(),
                    userProfile);

            result.merge(period, statistic.getValue(), Double::sum);
        }

        return result;
    }
}
