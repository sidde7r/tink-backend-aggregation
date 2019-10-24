package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;

public interface StringMaskerBuilder {
    ImmutableList<String> getValuesToMask();
}
