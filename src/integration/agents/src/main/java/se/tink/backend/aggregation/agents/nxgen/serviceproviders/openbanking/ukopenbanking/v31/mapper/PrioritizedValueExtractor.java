package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ComparatorUtils;

public class PrioritizedValueExtractor {

    public <T, VP> T pickByValuePriority(
            Collection<T> input, Function<T, VP> comparedValueExtractor, List<VP> valuePriorities) {
        Comparator<T> priorityComparator =
                ComparatorUtils.transformedComparator(
                        Ordering.explicit(valuePriorities), comparedValueExtractor::apply);

        return input.stream()
                .filter(b -> valuePriorities.contains(comparedValueExtractor.apply(b)))
                .min(priorityComparator)
                .orElseThrow(
                        () ->
                                constructMissingElementException(
                                        input, comparedValueExtractor, valuePriorities));
    }

    private <T, VP> NoSuchElementException constructMissingElementException(
            Collection<T> input, Function<T, VP> comparedValueExtractor, List<VP> valuePriorities) {

        String availableValues =
                input.stream()
                        .map(i -> comparedValueExtractor.apply(i).toString())
                        .collect(Collectors.joining(","));

        String preferredValues =
                valuePriorities.stream().map(Object::toString).collect(Collectors.joining(","));

        return new NoSuchElementException(
                "Could not find correct Value. Available values: "
                        + availableValues
                        + ". Accepted values: "
                        + preferredValues);
    }
}
