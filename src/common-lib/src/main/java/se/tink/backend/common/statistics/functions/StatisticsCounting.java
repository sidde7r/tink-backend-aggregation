package se.tink.backend.common.statistics.functions;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.core.Statistic;

/**
 * Reducer that calculates the sum of the statistics.
 */
public class StatisticsCounting {
    public static Collection<Statistic> count(Collection<Statistic> ss) {
        Statistic statistic = null;

        int count = 0;

        for (Statistic s : ss) {
            if (statistic == null) {
                statistic = Statistic.copyOf(s);
            }

            count++;
        }

        statistic.setValue(count);

        return Collections.singletonList(statistic);
    }
}
