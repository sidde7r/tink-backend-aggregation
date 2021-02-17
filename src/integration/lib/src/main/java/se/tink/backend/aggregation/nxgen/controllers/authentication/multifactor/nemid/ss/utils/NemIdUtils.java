package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdUtils {

    public static boolean valueMatchesAnyPattern(String value, List<Pattern> patterns) {
        return patterns.stream().map(p -> p.matcher(value)).anyMatch(Matcher::matches);
    }
}
