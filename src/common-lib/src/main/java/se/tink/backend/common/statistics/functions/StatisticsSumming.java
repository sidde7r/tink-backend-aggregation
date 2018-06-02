package se.tink.backend.common.statistics.functions;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.core.Statistic;

/**
 * Reducer that calculates the sum of the statistics.
 */
public class StatisticsSumming  {
    public static Collection<Statistic> sum(Collection<Statistic> ss) {
        Statistic statistic = null;

        for (Statistic s : ss) {
            if (statistic == null) {
                statistic = Statistic.copyOf(s);
            } else {
                statistic.setValue(statistic.getValue() + s.getValue());
            }
        }

        return Collections.singletonList(statistic);
    }

    public static Statistic reduce(Statistic s1, Statistic s2) {
        s1.setValue(s1.getValue() + s2.getValue());
        return s1;
    }
}
