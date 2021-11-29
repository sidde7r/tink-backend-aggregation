package se.tink.backend.aggregation.utils.masker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.regex.Pattern;
import se.tink.libraries.masker.MaskerPatternsProvider;

public class SensitiveValuesCollectionMaskerPatternsProvider implements MaskerPatternsProvider {

    private final ImmutableList<String> sensitiveValuesToMask;

    public SensitiveValuesCollectionMaskerPatternsProvider(
            Collection<String> sensitiveValuesToMask) {
        ImmutableSet<String> sensitiveValuesToMaskWithoutDuplicates =
                ImmutableSet.copyOf(sensitiveValuesToMask);
        this.sensitiveValuesToMask =
                ImmutableList.sortedCopyOf(
                        MaskingConstants.SENSITIVE_VALUES_SORTING_COMPARATOR,
                        sensitiveValuesToMaskWithoutDuplicates);
    }

    @Override
    public ImmutableList<Pattern> getPatternsToMask() {
        return sensitiveValuesToMask.stream()
                .map(s -> Pattern.compile(s, Pattern.LITERAL))
                .collect(ImmutableList.toImmutableList());
    }
}
