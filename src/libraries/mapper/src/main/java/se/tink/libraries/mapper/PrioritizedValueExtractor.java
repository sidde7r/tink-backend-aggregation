package se.tink.libraries.mapper;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.collections4.ComparatorUtils;

public class PrioritizedValueExtractor {

    public <T, VP> Optional<T> pickByValuePriority(
            Collection<T> input, Function<T, VP> comparedValueExtractor, List<VP> valuePriorities) {
        Comparator<T> priorityComparator =
                ComparatorUtils.transformedComparator(
                        Ordering.explicit(valuePriorities), comparedValueExtractor::apply);

        return input.stream()
                .filter(b -> valuePriorities.contains(comparedValueExtractor.apply(b)))
                .min(priorityComparator);
    }
}
