package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.utils.masker.StringMaskerBuilder;

public class SensitiveValuesCollectionStringMaskerBuilder implements StringMaskerBuilder {

    private final ImmutableList<String> sensitiveValuesToMask;

    public SensitiveValuesCollectionStringMaskerBuilder(Collection<String> sensitiveValuesToMask) {
        ImmutableSet<String> sensitiveValuesToMaskWithoutDuplicates =
                ImmutableSet.copyOf(sensitiveValuesToMask);
        this.sensitiveValuesToMask =
                ImmutableList.sortedCopyOf(
                        LogMasker.SENSITIVE_VALUES_SORTING_COMPARATOR,
                        sensitiveValuesToMaskWithoutDuplicates);
    }

    @Override
    public ImmutableList<Pattern> getValuesToMask() {
        return sensitiveValuesToMask.stream()
                .map(s -> Pattern.compile(s, Pattern.LITERAL))
                .collect(ImmutableList.toImmutableList());
    }
}
