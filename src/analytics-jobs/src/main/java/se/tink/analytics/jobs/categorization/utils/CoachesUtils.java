package se.tink.analytics.jobs.categorization.utils;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import se.tink.analytics.jobs.categorization.entities.Coach;

public class CoachesUtils {

    public static Map<String, Coach> getCoaches(Set<String> markets, Set<String> categoryTypes,
            Set<String> unhandledCategories, String workingDirectory) {

        if (markets == null || markets.isEmpty() || categoryTypes == null || categoryTypes.isEmpty()) {
            return null;
        }

        Map<String, Coach> coachByKey = Maps.newHashMap();

        for (String market : markets) {
            for (String categoryType : categoryTypes) {
                Coach c = new Coach(market, categoryType, unhandledCategories, workingDirectory);
                coachByKey.put(c.getKey(), c);
            }
        }

        return coachByKey;
    }
}
