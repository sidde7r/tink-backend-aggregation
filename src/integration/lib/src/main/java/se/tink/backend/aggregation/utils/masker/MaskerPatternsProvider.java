package se.tink.backend.aggregation.utils.masker;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public interface MaskerPatternsProvider {
    ImmutableList<Pattern> getPatternsToMask();
}
