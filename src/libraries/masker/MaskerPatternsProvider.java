package se.tink.libraries.masker;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public interface MaskerPatternsProvider {
    ImmutableList<Pattern> getPatternsToMask();
}
