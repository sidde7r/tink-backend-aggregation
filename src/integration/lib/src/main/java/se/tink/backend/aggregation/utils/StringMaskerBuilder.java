package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public interface StringMaskerBuilder {
    ImmutableList<Pattern> getValuesToMask();
}
