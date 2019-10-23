package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Comparator;

public class ClientConfigurationStringMaskerBuilder implements StringMaskerBuilder {

    private final ImmutableList<String> sensitiveValuesToMask;

    public ClientConfigurationStringMaskerBuilder(Collection<String> sensitiveValuesToMask) {
        ImmutableSet<String> sensitiveValuesToMaskWithoutDuplicates =
                ImmutableSet.copyOf(sensitiveValuesToMask);
        this.sensitiveValuesToMask =
                ImmutableList.sortedCopyOf(
                        Comparator.comparing(String::length)
                                .thenComparing(Comparator.naturalOrder())
                                .reversed(),
                        sensitiveValuesToMaskWithoutDuplicates);
    }

    @Override
    public ImmutableList<String> getValuesToMask() {
        return ImmutableList.copyOf(sensitiveValuesToMask);
    }
}
